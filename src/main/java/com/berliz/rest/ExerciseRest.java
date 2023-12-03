
package com.berliz.rest;

import com.berliz.DTO.ExerciseRequest;
import com.berliz.DTO.FileRequest;
import com.berliz.DTO.ProfilePhotoRequest;
import com.berliz.models.Exercise;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/exercise")
public interface ExerciseRest {

    /**
     * Add an exercise.
     *
     * @param exerciseRequest A map containing exercise details.
     * @return ResponseEntity containing a message about the result.
     */
    @PostMapping(path = "/add")
    ResponseEntity<String> addExercise(@ModelAttribute ExerciseRequest exerciseRequest);

    /**
     * Get a list of all exercises.
     *
     * @return ResponseEntity containing the list of all exercises.
     */
    @GetMapping(path = "/get")
    ResponseEntity<List<Exercise>> getAllExercises();

    /**
     * Get a list of active exercises.
     *
     * @return ResponseEntity containing the list of active exercises.
     */
    @GetMapping(path = "/getActiveExercises")
    ResponseEntity<List<Exercise>> getActiveExercises();

    /**
     * Get a specific exercise by ID.
     *
     * @param id The ID of the exercise to retrieve.
     * @return ResponseEntity containing the specific exercise.
     */
    @GetMapping(path = "/getExercise/{id}")
    ResponseEntity<Exercise> getExercise(@PathVariable Integer id);

    /**
     * Update an exercise image.
     *
     * @param fileRequest A map containing exercise details for the update.
     * @return ResponseEntity containing a message about the result.
     */
    @PutMapping(path = "/updateDemo")
    ResponseEntity<String> updateExerciseDemo(@ModelAttribute FileRequest fileRequest);

    /**
     * Update an exercise.
     *
     * @param requestMap A map containing exercise details for the update.
     * @return ResponseEntity containing a message about the result.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updateExercise(@RequestBody Map<String, String> requestMap);

    /**
     * Update the status of an exercise by ID.
     *
     * @param id The ID of the exercise to update.
     * @return ResponseEntity containing a message about the result.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    /**
     * Delete an exercise by ID.
     *
     * @param id The ID of the exercise to delete.
     * @return ResponseEntity containing a message about the result.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteExercise(@PathVariable Integer id);
}
