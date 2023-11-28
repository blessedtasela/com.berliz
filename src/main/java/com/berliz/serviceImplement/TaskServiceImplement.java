package com.berliz.serviceImplement;

import com.berliz.models.SubTask;
import com.berliz.models.Task;
import com.berliz.services.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TaskServiceImplement implements TaskService {
    @Override
    public ResponseEntity<String> addTask(Map<String, String> requestMap) {
        return null;
    }

    @Override
    public ResponseEntity<String> addSubTask(Map<String, String> requestMap) {
        return null;
    }

    @Override
    public ResponseEntity<List<Task>> getAllTasks() {
        return null;
    }

    @Override
    public ResponseEntity<List<SubTask>> getAllSubTasks() {
        return null;
    }

    @Override
    public ResponseEntity<List<Task>> getActiveTasks() {
        return null;
    }

    @Override
    public ResponseEntity<Task> getTask(Integer id) {
        return null;
    }

    @Override
    public ResponseEntity<String> updateTask(Map<String, String> requestMap) {
        return null;
    }

    @Override
    public ResponseEntity<String> updateSubTask(Map<String, String> requestMap) {
        return null;
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        return null;
    }

    @Override
    public ResponseEntity<String> deleteTask(Integer id) {
        return null;
    }

    @Override
    public ResponseEntity<String> deleteSubTask(Integer id) {
        return null;
    }
}
