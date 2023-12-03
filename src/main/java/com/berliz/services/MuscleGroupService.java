package com.berliz.services;

import com.berliz.DTO.FileRequest;
import com.berliz.DTO.MuscleGroupRequest;
import com.berliz.models.MuscleGroup;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface MuscleGroupService {
    ResponseEntity<String> addMuscleGroup(MuscleGroupRequest muscleGroupRequest) throws JsonProcessingException;

    ResponseEntity<List<MuscleGroup>> getAllMuscleGroups();

    ResponseEntity<List<MuscleGroup>> getActiveMuscleGroups();

    ResponseEntity<MuscleGroup> getMuscleGroup(Integer id);

    ResponseEntity<String> updateMuscleGroupImage(FileRequest imageRequest) throws JsonProcessingException;

    ResponseEntity<String> updateMuscleGroup(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<String> deleteMuscleGroup(Integer id) throws JsonProcessingException;
}
