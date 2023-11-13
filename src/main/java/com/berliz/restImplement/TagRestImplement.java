package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.Tag;
import com.berliz.rest.TagRest;
import com.berliz.services.TagService;
import com.berliz.utils.BerlizUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class TagRestImplement implements TagRest {

    @Autowired
    TagService tagService;

    /**
     * Add a new tag.
     *
     * @param requestMap A map containing tag information.
     * @return ResponseEntity representing the result of the tag addition.
     */
    @Override
    public ResponseEntity<String> addTag(Map<String, String> requestMap) {
        try {
            return tagService.addTag(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get a list of all tags.
     *
     * @return ResponseEntity containing a list of tags.
     */
    @Override
    public ResponseEntity<List<Tag>> getAllTags() {
        try {
            return tagService.getAllTags();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get a list of active tags.
     *
     * @return ResponseEntity containing a list of active tags.
     */
    @Override
    public ResponseEntity<List<Tag>> getActiveTags() {
        try {
            return tagService.getActiveTags();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Update an existing tag.
     *
     * @param requestMap A map containing tag information.
     * @return ResponseEntity representing the result of the tag update.
     */
    @Override
    public ResponseEntity<String> updateTag(Map<String, String> requestMap) {
        try {
            return tagService.updateTag(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Update the status of a tag.
     *
     * @param id The ID of the tag to update.
     * @return ResponseEntity representing the result of the status update.
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            return tagService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get a specific tag by its ID.
     *
     * @param id The ID of the tag to retrieve.
     * @return ResponseEntity containing the tag information.
     */
    @Override
    public ResponseEntity<?> getTag(Integer id) {
        try {
            return tagService.getTag(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Delete a tag by its ID.
     *
     * @param id The ID of the tag to delete.
     * @return ResponseEntity representing the result of the tag deletion.
     */
    @Override
    public ResponseEntity<String> deleteTag(Integer id) {
        try {
            return tagService.deleteTag(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
