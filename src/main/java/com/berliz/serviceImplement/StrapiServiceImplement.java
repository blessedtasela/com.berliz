package com.berliz.serviceImplement;

import com.berliz.DTO.StrapiPhotoMetadata;
import com.berliz.utils.MultipartInputStreamFileResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StrapiServiceImplement {

    @Value("${strapi.base-url}")
    private String strapiBaseUrl;

    @Value("${strapi.email}")
    private String email;

    @Value("${strapi.password}")
    private String password;

    private final RestTemplate restTemplate = new RestTemplate();
    private String jwtToken;
    private Instant tokenExpiry = Instant.now(); // force refresh on first use

    private static final String PHOTO_FOLDER_ID = "2"; // 🔐 Folder ID for "photos"

    // ✅ Authenticate and store token
    public String authenticateAndGetToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> loginPayload = Map.of(
                "identifier", email,
                "password", password
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginPayload, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                strapiBaseUrl + "/api/auth/local",
                request,
                String.class
        );

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            jwtToken = root.get("jwt").asText();
            tokenExpiry = Instant.now().plusSeconds(3600 * 24 * 7); // 7-day token lifespan
            return jwtToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to authenticate and extract token", e);
        }
    }

    // ✅ Auto-refresh token if expired
    public String getValidToken() {
        if (jwtToken == null || Instant.now().isAfter(tokenExpiry)) {
            return authenticateAndGetToken();
        }
        return jwtToken;
    }

    // ✅ Upload single photo
    public StrapiPhotoMetadata uploadPhoto(MultipartFile photoFile) throws IOException {
        try {
            return uploadPhotoWithToken(photoFile, getValidToken());
        } catch (HttpClientErrorException.Unauthorized e) {
            return uploadPhotoWithToken(photoFile, authenticateAndGetToken()); // Retry once
        }
    }

    // ✅ Upload photo to specific folder (photos)
    private StrapiPhotoMetadata uploadPhotoWithToken(MultipartFile file, String token) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
        body.add("folder", PHOTO_FOLDER_ID); // ✅ Place in "photos" folder

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                strapiBaseUrl + "/api/upload",
                requestEntity,
                String.class
        );

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response.getBody()).get(0); // Strapi returns an array

        StrapiPhotoMetadata metadata = new StrapiPhotoMetadata();
        metadata.setPhotoUrl(node.get("url").asText());
        metadata.setName(node.get("name").asText());
        metadata.setMimeType(node.get("mime").asText());
        metadata.setByteSize(node.get("size").asLong());
        metadata.setCaption(""); // optional

        return metadata;
    }

    // ✅ Upload multiple photos
    public List<StrapiPhotoMetadata> uploadMultiplePhotos(MultipartFile[] files) throws IOException {
        List<StrapiPhotoMetadata> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(uploadPhoto(file));
        }
        return results;
    }

    public void deletePhotoFromStrapi(Integer strapiPhotoId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getValidToken());

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(
                    strapiBaseUrl + "/api/upload/files/" + strapiPhotoId,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );
        } catch (HttpClientErrorException.NotFound e) {
            // Log or ignore if already deleted
            System.out.println("Strapi file not found for deletion: " + strapiPhotoId);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting photo from Strapi", e);
        }
    }

    public void updatePhotoMetadataInStrapi(Integer strapiPhotoId, String newName, String newCaption) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getValidToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> updates = Map.of(
                    "name", newName,
                    "caption", newCaption
            );

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(updates, headers);

            restTemplate.exchange(
                    strapiBaseUrl + "/api/upload/files/" + strapiPhotoId,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Error updating photo metadata in Strapi", e);
        }
    }


    public StrapiPhotoMetadata getPhotoFromStrapiById(Integer photoId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getValidToken());

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // URL for a single photo by its ID
            String url = strapiBaseUrl + "/api/upload/files/" + photoId;

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody()).get("data");

            if (node == null) {
                throw new RuntimeException("Photo not found with ID: " + photoId);
            }

            JsonNode attributes = node.get("attributes");
            StrapiPhotoMetadata photo = new StrapiPhotoMetadata();
            photo.setId(node.get("id").asInt());
            photo.setName(attributes.get("name").asText());
            photo.setCaption(attributes.has("caption") ? attributes.get("caption").asText() : "");
            photo.setMimeType(attributes.get("mime").asText());
            photo.setByteSize((long) attributes.get("size").asDouble());
            photo.setPhotoUrl(attributes.get("url").asText());

            return photo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve photo from Strapi", e);
        }
    }

    public List<StrapiPhotoMetadata> getPhotosFromStrapiByIds(List<Integer> photoIds) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getValidToken());

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Generate the URL to fetch multiple photos using an array of IDs
            StringBuilder urlBuilder = new StringBuilder(strapiBaseUrl + "/api/upload/files?filters[id][$in]=");
            for (int i = 0; i < photoIds.size(); i++) {
                urlBuilder.append(photoIds.get(i));
                if (i < photoIds.size() - 1) {
                    urlBuilder.append(",");
                }
            }

            // Add pagination or other parameters as needed
            String url = urlBuilder.toString();
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(response.getBody()).get("data");

            List<StrapiPhotoMetadata> photoList = new ArrayList<>();
            for (JsonNode node : data) {
                JsonNode attributes = node.get("attributes");
                StrapiPhotoMetadata photo = new StrapiPhotoMetadata();
                photo.setId(node.get("id").asInt());
                photo.setName(attributes.get("name").asText());
                photo.setCaption(attributes.has("caption") ? attributes.get("caption").asText() : "");
                photo.setMimeType(attributes.get("mime").asText());
                photo.setByteSize((long) attributes.get("size").asDouble());
                photo.setPhotoUrl(attributes.get("url").asText());
                photoList.add(photo);
            }

            return photoList;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve photos from Strapi", e);
        }
    }

    public void deleteMultiplePhotosFromStrapi(List<Integer> strapiPhotoIds) {
        for (Integer id : strapiPhotoIds) {
            deletePhotoFromStrapi(id);
        }
    }


}
