package com.berliz.services;

import com.berliz.DTO.ExerciseRequest;
import com.berliz.DTO.FileRequest;
import com.berliz.DTO.ProfilePhotoRequest;
import com.berliz.models.Exercise;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface ExerciseService {
    public ResponseEntity<String> addExercise(ExerciseRequest exerciseRequest) throws JsonProcessingException;

    public ResponseEntity<List<Exercise>> getAllExercises();

    public ResponseEntity<List<Exercise>> getActiveExercises();

    public ResponseEntity<Exercise> getExercise(Integer id);

    public ResponseEntity<String> updateExerciseDemo(FileRequest fileRequest) throws JsonProcessingException;

    public ResponseEntity<String> updateExercise(Map<String, String> requestMap) throws JsonProcessingException;

    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    public ResponseEntity<String> deleteExercise(Integer id) throws JsonProcessingException;
}
