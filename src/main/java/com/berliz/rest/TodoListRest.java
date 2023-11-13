package com.berliz.rest;

import com.berliz.models.Tag;
import com.berliz.models.TodoList;
import com.berliz.models.Trainer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/todoList")
public interface TodoListRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addTodo(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @GetMapping(path = "/get")
    ResponseEntity<List<TodoList>> getAllTodos() throws JsonProcessingException;

    @PutMapping(path = "/update")
    ResponseEntity<String> updateTodo( Map<String, String> requestMap) throws JsonProcessingException;

    @PutMapping(path = "/updateStatus")
    ResponseEntity<String> updateStatus(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteTodo(@PathVariable Integer id) throws JsonProcessingException;
}
