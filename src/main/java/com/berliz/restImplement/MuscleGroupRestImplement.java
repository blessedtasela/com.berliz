package com.berliz.restImplement;

import com.berliz.DTO.ImageRequest;
import com.berliz.DTO.MuscleGroupRequest;
import com.berliz.DTO.ProfilePhotoRequest;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.MuscleGroup;
import com.berliz.rest.MuscleGroupRest;
import com.berliz.services.MuscleGroupService;
import com.berliz.utils.BerlizUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class MuscleGroupRestImplement implements MuscleGroupRest {

    @Autowired
    MuscleGroupService muscleGroupService;

    @Override
    public ResponseEntity<String> addMuscleGroup(MuscleGroupRequest muscleGroupRequest) {
        try {
            return muscleGroupService.addMuscleGroup(muscleGroupRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<MuscleGroup>> getAllMuscleGroups() {
        try {
            return muscleGroupService.getAllMuscleGroups();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<MuscleGroup>> getActiveMuscleGroups() {
        try {
            return muscleGroupService.getActiveMuscleGroups();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MuscleGroup> getMuscleGroup(Integer id) {
        try {
            return muscleGroupService.getMuscleGroup(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new MuscleGroup(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateMuscleGroupImage(ImageRequest imageRequest) {
        try {
            return muscleGroupService.updateMuscleGroupImage(imageRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateMuscleGroup(Map<String, String> requestMap) {
        try {
            return muscleGroupService.updateMuscleGroup(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            return muscleGroupService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteMuscleGroup(Integer id) {
        try {
            return muscleGroupService.deleteMuscleGroup(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
