package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.TodoList;
import com.berliz.rest.TodoListRest;
import com.berliz.services.TodoListService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TodoListRestImplement implements TodoListRest {

    @Autowired
    TodoListService todoListService;

    @Override
    public ResponseEntity<String> addTodo(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return todoListService.addTodo(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TodoList>> getAllTodos() throws JsonProcessingException {
        try {
            return todoListService.getAllTodos();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateTodo(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return todoListService.updateTodo(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return todoListService.updateStatus(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTodo(Integer id) throws JsonProcessingException {
        try {
            return todoListService.deleteTodo(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }
}
