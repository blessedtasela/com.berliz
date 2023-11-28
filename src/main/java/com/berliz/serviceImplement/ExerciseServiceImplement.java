package com.berliz.serviceImplement;

import com.berliz.DTO.ExerciseRequest;
import com.berliz.DTO.ProfilePhotoRequest;
import com.berliz.models.Exercise;
import com.berliz.services.ExerciseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ExerciseServiceImplement implements ExerciseService {
    @Override
    public ResponseEntity<String> addExercise(ExerciseRequest exerciseRequest) {
        return null;
    }

    @Override
    public ResponseEntity<List<Exercise>> getAllExercises() {
        return null;
    }

    @Override
    public ResponseEntity<List<Exercise>> getActiveExercises() {
        return null;
    }

    @Override
    public ResponseEntity<Exercise> getExercise(Integer id) {
        return null;
    }

    @Override
    public ResponseEntity<String> updateExerciseImage(ProfilePhotoRequest photoRequest) {
        return null;
    }

    @Override
    public ResponseEntity<String> updateExercise(Map<String, String> requestMap) {
        return null;
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        return null;
    }

    @Override
    public ResponseEntity<String> deleteExercise(Integer id) {
        return null;
    }
}
