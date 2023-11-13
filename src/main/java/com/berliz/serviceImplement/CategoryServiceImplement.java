package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Category;
import com.berliz.models.CategoryLike;
import com.berliz.models.Tag;
import com.berliz.models.User;
import com.berliz.repository.CategoryLikeRepo;
import com.berliz.repository.CategoryRepo;
import com.berliz.repository.TagRepo;
import com.berliz.repository.UserRepo;
import com.berliz.services.CategoryService;
import com.berliz.utils.BerlizUtilities;
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
public class CategoryServiceImplement implements CategoryService {

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    TagRepo tagRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    UserRepo userRepo;

    @Autowired
    CategoryLikeRepo categoryLikeRepo;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;


    /**
     * Adds a new category based on the provided requestMap.
     *
     * @param requestMap A map containing category information such as name, description, photo, likes, and tagIds.
     * @return A ResponseEntity indicating the result of the category addition operation.
     * @throws JsonProcessingException If there is an error processing the JSON data.
     */
    @Override
    public ResponseEntity<String> addCategory(Map<String, String> requestMap) throws JsonProcessingException {
        log.info("Inside addCategory {}", requestMap);
        try {
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            if (!validateCategoryMap(requestMap, false)) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }
            Category category = categoryRepo.findByName(requestMap.get("name"));
            if (category != null) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Category exists");
            }
            categoryRepo.save(getCategoryFromMap(requestMap));
            return buildResponse(HttpStatus.OK, "Category added successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieves a list of all categories from the database.
     *
     * @return A ResponseEntity containing the list of categories if successful, or an empty list if there was an error.
     */
    @Override
    public ResponseEntity<List<Category>> getCategories() {
        try {
            log.info("Inside getCategories 'ADMIN'");
            return new ResponseEntity<>(categoryRepo.findAll(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Retrieves a list of all categories that their status is true from the database.
     *
     * @return A ResponseEntity containing the list of categories if successful, or an empty list if there was an error.
     */
    @Override
    public ResponseEntity<List<Category>> getActiveCategories() {
        try {
            log.info("Inside getAllCategories 'USER'");
            return new ResponseEntity<>(categoryRepo.getActiveCategories(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Updates a category with the provided information.
     *
     * @param requestMap A map containing the category update information.
     *                   It should include the following keys:
     *                   - "id": The ID of the category to be updated.
     *                   - "name": The updated name of the category.
     *                   - "description": The updated description of the category.
     *                   - "photo": The updated photo URL of the category.
     *                   - "likes": The updated number of likes for the category.
     *                   - "tagIds": A comma-separated string of updated tag IDs associated with the category.
     * @return A ResponseEntity containing a status code and a message indicating the result of the update operation.
     * @throws JsonProcessingException If there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateCategory {}", requestMap);

            // Check if the user is an admin
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            // Validate the request data
            boolean isValid = validateCategoryMap(requestMap, true);
            log.info("Is request valid? {}", isValid);
            if (!isValid) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            // Extract category ID from the request
            Integer id = Integer.parseInt(requestMap.get("id"));

            // Check if the category with the given ID exists
            Optional<Category> optional = categoryRepo.findById(id);
            log.info("Does category exist? {}", optional);
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "Category ID not found");
            }

            // Get the existing category
            Category existingCategory = optional.get();

            // Save the existing date value
            Date existingDate = existingCategory.getDate();

            // Update category attributes
            existingCategory.setName(requestMap.get("name"));
            existingCategory.setDescription(requestMap.get("description"));
            existingCategory.setPhoto(requestMap.get("photo"));
            existingCategory.setLikes(Integer.parseInt(requestMap.get("likes")));
            existingCategory.setLastUpdate(new Date());
            existingCategory.setDate(existingDate);

            // Create a set to store the updated tags
            String tagIdsString = requestMap.get("tagIds");
            String[] tagIdsArray = tagIdsString.split(",");

            Set<Tag> updatedTags = new HashSet<>();
            for (String tagIdString : tagIdsArray) {
                // Remove leading and trailing spaces before parsing
                int tagId = Integer.parseInt(tagIdString.trim());
                Tag tag = tagRepo.findById(tagId).orElse(null);
                if (tag != null) {
                    updatedTags.add(tag); // Add the tag to the set of updated tags
                }
            }

            // Set the updated tags for the existingCategory
            existingCategory.setTagSet(updatedTags);

            // Save the updated category
            categoryRepo.save(existingCategory);
            simpMessagingTemplate.convertAndSend("/topic/updateCategory", existingCategory);
            return buildResponse(HttpStatus.OK, "Category updated successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Deletes a category with the specified ID.
     *
     * @param id The ID of the category to be deleted.
     * @return A ResponseEntity containing a message indicating the result of the deletion operation.
     * @throws JsonProcessingException if an error occurs during JSON processing.
     */
    @Override
    public ResponseEntity<String> deleteCategory(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteCategory {}", id);
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Category> optional = categoryRepo.findById(id);
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "Category id not found");
            }
            log.info("inside optional {}", id);
            Category category = optional.get();
            if (category.getStatus().equalsIgnoreCase("true")) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Category iis active, cannot complete request");
            }
            categoryRepo.deleteById(id);
            simpMessagingTemplate.convertAndSend("/topic/deleteCategory", category);
            return buildResponse(HttpStatus.OK, "Category deleted successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Updates the status (activation or deactivation) of a category with the provided ID.
     *
     * @param id The ID of the category for which to update the status.
     * @return A ResponseEntity containing a status code and a message indicating the result of the status update operation.
     * @throws JsonProcessingException If there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateStatus {}", id);
            String status;
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Category> optional = categoryRepo.findById(id);
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "Category ID not found");
            }
            log.info("Inside optional {}", optional);
            status = optional.get().getStatus();
            Category category = optional.get();
            String responseMessage;
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                responseMessage = "Category Status updated successfully. Now Deactivated";
            } else {
                status = "true";
                responseMessage = "Category Status updated successfully. Now Activated";
            }

            category.setStatus(status);
            categoryRepo.save(category);
            simpMessagingTemplate.convertAndSend("/topic/updateCategoryStatus", category);
            return buildResponse(HttpStatus.OK, responseMessage);
        } catch (
                Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieves a category by its unique ID.
     *
     * @param id The ID of the category to retrieve.
     * @return A ResponseEntity containing the category information if found, or a bad request response if the category ID is not found.
     */
    @Override
    public ResponseEntity<?> getCategory(Integer id) {
        try {
            log.info("Inside getBrand {}", id);
            Optional<Category> optional = categoryRepo.findById(id);
            if (optional.isPresent()) {
                return ResponseEntity.ok(optional);
            } else {
                return ResponseEntity.badRequest().body("Category ID not found");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
    }

    /**
     * Retrieves a list of categories associated with a specific tag ID.
     *
     * @param id The ID of the tag to filter categories.
     * @return A ResponseEntity containing the list of categories associated with the specified tag if found, or a bad request response if the tag ID is not found in any categories.
     */
    @Override
    public ResponseEntity<List<Category>> getByTag(Integer id) {
        try {
            log.info("Inside getBrand {}", id);
            List<Category> category = categoryRepo.getByTag(id);
            if (category != null) {
                log.info("Inside optional {}", category);
                return new ResponseEntity<>(category, HttpStatus.OK);
            } else {
                return new ResponseEntity("Tag ID not found in any Category", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> likeCategory(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside likeTrainer {}", id);
            Optional<Category> optional = categoryRepo.findById(id);
            User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
            boolean validUser = jwtFilter.isBerlizUser();

            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Category id not found");
            }
            Category category = optional.get();
            boolean hasLiked = categoryLikeRepo.existsByUserAndCategory(user, category);

            if (hasLiked) {
                // dislike category
                categoryLikeRepo.deleteByUserAndCategory(user, category);
                category.setLikes(category.getLikes() - 1);
                categoryRepo.save(category);
                simpMessagingTemplate.convertAndSend("/topic/likeCategory", category);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Hello, " + user.getFirstname() + " you have disliked " + category.getName() + " category");

            } else {
                // like category
                CategoryLike categoryLike = new CategoryLike();
                categoryLike.setUser(user);
                categoryLike.setCategory(category);
                categoryLike.setDate(new Date());
                categoryLikeRepo.save(categoryLike);
                category.setLikes(category.getLikes() + 1);
                categoryRepo.save(category);
                simpMessagingTemplate.convertAndSend("/topic/likeCategory", category);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Hello, " +
                        user.getFirstname() + " you just liked " + category.getName() + " category");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Validates whether a given request map contains the required fields for a category.
     *
     * @param requestMap The map containing request parameters.
     * @param validId    Whether a valid category ID is required in the map.
     * @return True if the request map contains all required fields; false otherwise.
     */
    private boolean validateCategoryMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("description")
                    && requestMap.containsKey("photo")
                    && requestMap.containsKey("tagIds")
                    && requestMap.containsKey("likes");
        } else {
            return requestMap.containsKey("name")
                    && requestMap.containsKey("description")
                    && requestMap.containsKey("photo")
                    && requestMap.containsKey("tagIds")
                    && requestMap.containsKey("likes");
        }
    }

    /**
     * Creates a Category object from the provided request map containing category data.
     *
     * @param requestMap The map containing category data as key-value pairs.
     * @return A Category object created from the provided map.
     */
    private Category getCategoryFromMap(Map<String, String> requestMap) {
        Category category = new Category();
        Date currentDate = new Date();

        // Parse tagIds as a comma-separated string
        String tagIdsString = requestMap.get("tagIds");
        String[] tagIdsArray = tagIdsString.split(",");

        Set<Tag> tagSet = new HashSet<>();
        for (String tagIdString : tagIdsArray) {
            // Remove leading and trailing spaces before parsing
            int tagId = Integer.parseInt(tagIdString.trim());

            Tag tag = new Tag();
            tag.setId(tagId);
            tagSet.add(tag);
        }

        category.setDate(currentDate);
        category.setName(requestMap.get("name"));
        category.setDescription(requestMap.get("description"));
        category.setPhoto(requestMap.get("photo"));
        category.setLikes(Integer.parseInt(requestMap.get("likes")));
        category.setStatus("true");
        category.setLastUpdate(currentDate);
        category.setTagSet(tagSet);
        simpMessagingTemplate.convertAndSend("/topic/getCategoryFromMap", category);
        return category;
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
