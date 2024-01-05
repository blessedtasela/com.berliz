package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.SubTask;
import com.berliz.models.Task;
import com.berliz.rest.TaskRest;
import com.berliz.services.TaskService;
import com.berliz.utils.BerlizUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class TaskRestImplement implements TaskRest {

    @Autowired
    TaskService taskService;

    @Override
    public ResponseEntity<String> addTask(Map<String, String> requestMap) {
        try {
            return taskService.addTask(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addSubTask(Map<String, String> requestMap) {
        try {
            return taskService.addSubTask(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Task>> getAllTasks() {
        try {
            return taskService.getAllTasks();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<List<SubTask>> getAllSubTasks() {
        try {
            return taskService.getAllSubTasks();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Task>> getActiveTasks() {
        try {
            return taskService.getActiveTasks();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Task>> getTrainerTasks() {
        try {
            return taskService.getTrainerTasks();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Task>> getClientTasks() {
        try {
            return taskService.getClientTasks();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Task> getTask(Integer id) {
        try {
            return taskService.getTask(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Task(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateTask(Map<String, String> requestMap) {
        try {
            return taskService.updateTask(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateSubTask(Map<String, String> requestMap) {
        try {
            return taskService.updateSubTask(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            return taskService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteTask(Integer id) {
        try {
            return taskService.deleteTask(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteSubTask(Integer id) {
        try {
            return taskService.deleteSubTask(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
