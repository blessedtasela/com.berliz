
package com.berliz.rest;

import com.berliz.DTO.ImageRequest;
import com.berliz.DTO.MuscleGroupRequest;
import com.berliz.DTO.ProfilePhotoRequest;
import com.berliz.models.MuscleGroup;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/muscleGroup")
public interface MuscleGroupRest {

    /**
     * Add a muscle group.
     *
     * @param muscleGroupRequest A map containing muscle group details.
     * @return ResponseEntity containing a message about the result.
     */
    @PostMapping(path = "/add")
    ResponseEntity<String> addMuscleGroup(@ModelAttribute MuscleGroupRequest muscleGroupRequest);

    /**
     * Get a list of all muscle groups.
     *
     * @return ResponseEntity containing the list of all muscle groups.
     */
    @GetMapping(path = "/get")
    ResponseEntity<List<MuscleGroup>> getAllMuscleGroups();

    /**
     * Get a list of active muscle groups.
     *
     * @return ResponseEntity containing the list of active muscle groups.
     */
    @GetMapping(path = "/getActiveMuscleGroups")
    ResponseEntity<List<MuscleGroup>> getActiveMuscleGroups();

    /**
     * Get a specific muscle group by ID.
     *
     * @param id The ID of the muscle group to retrieve.
     * @return ResponseEntity containing the specific muscle group.
     */
    @GetMapping(path = "/getMuscleGroup/{id}")
    ResponseEntity<MuscleGroup> getMuscleGroup(@PathVariable Integer id);

    /**
     * Update a muscle group Image.
     *
     * @param imageRequest A map containing muscle group details for the update.
     * @return ResponseEntity containing a message about the result.
     */
    @PutMapping(path = "/updateImage")
    ResponseEntity<String> updateMuscleGroupImage(@ModelAttribute ImageRequest imageRequest);


    /**
     * Update a muscle group.
     *
     * @param requestMap A map containing muscle group details for the update.
     * @return ResponseEntity containing a message about the result.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updateMuscleGroup(@RequestBody Map<String, String> requestMap);

    /**
     * Update the status of a muscle group by ID.
     *
     * @param id The ID of the muscle group to update.
     * @return ResponseEntity containing a message about the result.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    /**
     * Delete a muscle group by ID.
     *
     * @param id The ID of the muscle group to delete.
     * @return ResponseEntity containing a message about the result.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteMuscleGroup(@PathVariable Integer id);
}