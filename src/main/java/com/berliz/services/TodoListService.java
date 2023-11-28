package com.berliz.services;

import com.berliz.models.Tag;
import com.berliz.models.TodoList;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface TodoListService {
    ResponseEntity<String> addTodo(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<TodoList>> getAllTodos();

    ResponseEntity<List<TodoList>> getMyTodo();

    ResponseEntity<String> updateTodo(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id, String status) throws JsonProcessingException;

    ResponseEntity<String> deleteTodo(Integer id) throws JsonProcessingException;

}
