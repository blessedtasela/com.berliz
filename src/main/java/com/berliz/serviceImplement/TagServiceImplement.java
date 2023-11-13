package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Tag;
import com.berliz.models.Trainer;
import com.berliz.repository.TagRepo;
import com.berliz.services.TagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class TagServiceImplement implements TagService {

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    TagRepo tagRepo;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Adds a new tag based on the provided request map.
     *
     * @param requestMap A map containing the tag information to be added.
     *                   The map should include at least a "name" field.
     * @return A ResponseEntity with a status code and a message indicating the result of the tag addition.
     * @throws JsonProcessingException If there is an issue with processing JSON data.
     */
    @Override
    public ResponseEntity<String> addTag(Map<String, String> requestMap) throws JsonProcessingException {
        log.info("Inside addTag {}", requestMap);
        try {
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            if (validateTagMap(requestMap, false)) {
                Tag tag = tagRepo.findByName(requestMap.get("name"));
                if (tag != null) {
                    return buildResponse(HttpStatus.BAD_REQUEST, "Tag exists");
                }
                tagRepo.save(getTagFromMap(requestMap, false));
                return buildResponse(HttpStatus.OK, "Tag added successfully");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieves a list of all tags.
     *
     * @return A ResponseEntity containing a list of Tag objects with a status code indicating the result.
     */
    @Override
    public ResponseEntity<List<Tag>> getAllTags() {
        try {
            log.info("inside if block for getAllTags{}");
            return new ResponseEntity<List<Tag>>(tagRepo.getAllTags(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Update a tag based on the provided request map.
     *
     * @param requestMap A map containing tag data, including "id," "name," and "description."
     * @return A ResponseEntity with a status code and a message indicating the result of the update operation.
     * @throws JsonProcessingException If there is an issue with JSON processing.
     */
    @Override
    public ResponseEntity<String> updateTag(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("inside updateTag {}", requestMap);
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            boolean isValid = validateTagMap(requestMap, true);
            log.info("Is request valid? {}", isValid);
            if (!isValid) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }
            Optional<Tag> optional = tagRepo.findById(Integer.parseInt(requestMap.get("id")));
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "Tag id not found");
            }
            log.info("inside optional {}", requestMap);
            Tag tag = optional.get();
            tag.setName(requestMap.get("name"));
            tag.setDescription(requestMap.get("description"));
            tag.setLastUpdate(new Date());
            tagRepo.save(tag);
            simpMessagingTemplate.convertAndSend("/topic/updateTag", tag);
            return buildResponse(HttpStatus.OK, "Tag updated successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Update the status of a tag based on its ID.
     *
     * @param id The ID of the tag to update.
     * @return A ResponseEntity with a status message indicating the result of the status update.
     * @throws JsonProcessingException If there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            String status;
            log.info("Inside updateStatus {}", id);
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Tag> optional = tagRepo.findById(id);
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "Tag id not found");
            }
            log.info("Inside optional {}", optional);
            status = optional.get().getStatus();
            String responseMessage;
            Tag tag = optional.get();

            if (status.equalsIgnoreCase("true")) {
                status = "false";
            responseMessage = "Tag has been deactivated successfully";
            } else {
                status = "true";
                responseMessage =  "Tag has been successfully activated";
            }

            tag.setStatus(status);
            tagRepo.save(tag);
            simpMessagingTemplate.convertAndSend("/topic/updateTagStatus", tag);
            return buildResponse(HttpStatus.OK, responseMessage);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieve a tag by its ID.
     *
     * @param id The ID of the tag to retrieve.
     * @return A ResponseEntity containing the tag if found, or a bad request response if the tag is not found.
     */
    @Override
    public ResponseEntity<?> getTag(Integer id) {
        try {
            log.info("Inside getById {}", id);
            Optional<Tag> optional = tagRepo.findById(id);
            if (optional.isPresent()) {
                log.info("Inside optional {}", id);
                return ResponseEntity.ok(optional);
            } else {
                return ResponseEntity.badRequest().body("Tag id not found");
            }
        } catch (Exception ex) {
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
    }

    /**
     * Delete a tag by its ID.
     *
     * @param id The ID of the tag to delete.
     * @return A ResponseEntity indicating the success or failure of the delete operation.
     * @throws JsonProcessingException If there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> deleteTag(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteTag {}", id);
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Tag> optional = tagRepo.findById(id);
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "Tag id not found");
            }
            log.info("inside optional {}", id);
            Tag tag = optional.get();
            tagRepo.deleteById(id);
            simpMessagingTemplate.convertAndSend("/topic/deleteTag", tag);
            return buildResponse(HttpStatus.OK, "Tag deleted successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Tag>> getActiveTags() {
        try {
            log.info("Inside getActiveTags");
            List<Tag> tags = tagRepo.getActiveTags();
            return new ResponseEntity<>(tags, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Validate a map of tag properties.
     *
     * @param requestMap A map containing tag properties, including "name" and "description."
     * @param validId    A boolean indicating whether an "id" property should be validated.
     * @return True if the map is valid based on the presence of required properties, false otherwise.
     */
    private boolean validateTagMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("description");
        } else {
            return requestMap.containsKey("name")
                    && requestMap.containsKey("description");
        }
    }

    private Tag getTagFromMap(Map<String, String> requestMap, Boolean isAdd) {
        Tag tag = new Tag();
        Date currentDate = new Date();
        if (isAdd) {
            tag.setId(Integer.parseInt(requestMap.get("id")));
        }
        tag.setDate(currentDate);
        tag.setName(requestMap.get("name"));
        tag.setDescription(requestMap.get("description"));
        tag.setStatus("true");
        tag.setLastUpdate(currentDate);

        simpMessagingTemplate.convertAndSend("/topic/getTagFromMap", tag);
        return tag;
    }

    /**
     * Build a ResponseEntity with the given status code and message.
     *
     * @param status  The HTTP status code.
     * @param message The response message.
     * @return A ResponseEntity with the specified status and message.
     * @throws JsonProcessingException If there is an issue processing the JSON response.
     */
    private ResponseEntity<String> buildResponse(HttpStatus status, String message) throws JsonProcessingException {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", message);

        ObjectMapper objectMapper = new ObjectMapper();
        String responseBodyJson = objectMapper.writeValueAsString(responseBody);

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBodyJson);
    }
}
