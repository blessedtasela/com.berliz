package com.berliz.services;

import com.berliz.models.SubTask;
import com.berliz.models.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface TaskService {
    ResponseEntity<String> addTask(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> addSubTask(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<Task>> getAllTasks();

    ResponseEntity<List<SubTask>> getAllSubTasks();

    ResponseEntity<List<Task>> getActiveTasks();

    ResponseEntity<Task> getTask(Integer id);

    ResponseEntity<String> updateTask(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateSubTask(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<String> deleteTask(Integer id) throws JsonProcessingException;

    ResponseEntity<String> deleteSubTask(Integer id) throws JsonProcessingException;

    ResponseEntity<List<Task>> getTrainerTasks();
}
