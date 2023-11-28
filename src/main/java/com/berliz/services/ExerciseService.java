package com.berliz.services;

import com.berliz.DTO.ExerciseRequest;
import com.berliz.DTO.ProfilePhotoRequest;
import com.berliz.models.Exercise;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface ExerciseService {
    public ResponseEntity<String> addExercise(ExerciseRequest exerciseRequest);

    public ResponseEntity<List<Exercise>> getAllExercises();

    public ResponseEntity<List<Exercise>> getActiveExercises();

    public ResponseEntity<Exercise> getExercise(Integer id);

    public ResponseEntity<String> updateExerciseImage(ProfilePhotoRequest photoRequest);

    public ResponseEntity<String> updateExercise(Map<String, String> requestMap);

    public ResponseEntity<String> updateStatus(Integer id);

    public ResponseEntity<String> deleteExercise(Integer id);
}
