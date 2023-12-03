package com.berliz.restImplement;

import com.berliz.DTO.ExerciseRequest;
import com.berliz.DTO.FileRequest;
import com.berliz.DTO.ProfilePhotoRequest;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Exercise;
import com.berliz.rest.ExerciseRest;
import com.berliz.services.ExerciseService;
import com.berliz.utils.BerlizUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ExerciseRestImplement implements ExerciseRest {

    @Autowired
    ExerciseService exerciseService;

    @Override
    public ResponseEntity<String> addExercise(ExerciseRequest exerciseRequest) {
        try {
            return exerciseService.addExercise(exerciseRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
}

    @Override
    public ResponseEntity<List<Exercise>> getAllExercises() {
        try {
            return exerciseService.getAllExercises();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Exercise>> getActiveExercises() {
        try {
            return exerciseService.getActiveExercises();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Exercise> getExercise(Integer id) {
        try {
            return exerciseService.getExercise(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Exercise(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateExerciseDemo(FileRequest fileRequest) {
        try {
            return exerciseService.updateExerciseDemo(fileRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<String> updateExercise(Map<String, String> requestMap) {
        try {
            return exerciseService.updateExercise(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            return exerciseService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteExercise(Integer id) {
        try {
            return exerciseService.deleteExercise(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
