package com.berliz.services;

import com.berliz.models.SubTask;
import com.berliz.models.Task;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface TaskService {
    ResponseEntity<String> addTask(Map<String, String> requestMap);

    ResponseEntity<String> addSubTask(Map<String, String> requestMap);

    ResponseEntity<List<Task>> getAllTasks();

    ResponseEntity<List<SubTask>> getAllSubTasks();

    ResponseEntity<List<Task>> getActiveTasks();

    ResponseEntity<Task> getTask(Integer id);

    ResponseEntity<String> updateTask(Map<String, String> requestMap);

    ResponseEntity<String> updateSubTask(Map<String, String> requestMap);

    ResponseEntity<String> updateStatus(Integer id);

    ResponseEntity<String> deleteTask(Integer id);

    ResponseEntity<String> deleteSubTask(Integer id);
}
