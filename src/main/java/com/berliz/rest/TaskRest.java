
package com.berliz.rest;

import com.berliz.models.SubTask;
import com.berliz.models.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/task")
public interface TaskRest {

    /**
     * Add a task.
     *
     * @param requestMap A map containing task details.
     * @return ResponseEntity containing a message about the result.
     */
    @PostMapping(path = "/add")
    ResponseEntity<String> addTask(@RequestBody Map<String, String> requestMap);

    /**
     * Add a subtask.
     *
     * @param requestMap A map containing subtask details.
     * @return ResponseEntity containing a message about the result.
     */
    @PostMapping(path = "/addSubTask")
    ResponseEntity<String> addSubTask(@RequestBody Map<String, String> requestMap);

    /**
     * Get a list of all tasks.
     *
     * @return ResponseEntity containing the list of all tasks.
     */
    @GetMapping(path = "/get")
    ResponseEntity<List<Task>> getAllTasks();

    /**
     * Get a list of all subtasks.
     *
     * @return ResponseEntity containing the list of all subtasks.
     */
    @GetMapping(path = "/getSubTasks")
    ResponseEntity<List<SubTask>> getAllSubTasks();

    /**
     * Get a list of active tasks.
     *
     * @return ResponseEntity containing the list of active tasks.
     */
    @GetMapping(path = "/getActiveTasks")
    ResponseEntity<List<Task>> getActiveTasks();

    /**
     * Get a list of active tasks.
     *
     * @return ResponseEntity containing the list of active tasks.
     */
    @GetMapping(path = "/getTrainerTasks")
    ResponseEntity<List<Task>> getTrainerTasks();

    /**
     * Get a specific task by ID.
     *
     * @param id The ID of the task to retrieve.
     * @return ResponseEntity containing the specific task.
     */
    @GetMapping(path = "/getTask/{id}")
    ResponseEntity<Task> getTask(@PathVariable Integer id);

    /**
     * Update a task.
     *
     * @param requestMap A map containing task details for the update.
     * @return ResponseEntity containing a message about the result.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updateTask(@RequestBody Map<String, String> requestMap);

    /**
     * Update a subtask.
     *
     * @param requestMap A map containing subtask details for the update.
     * @return ResponseEntity containing a message about the result.
     */
    @PutMapping(path = "/updateSubTask")
    ResponseEntity<String> updateSubTask(@RequestBody Map<String, String> requestMap);

    /**
     * Update the status of a task by ID.
     *
     * @param id The ID of the task to update.
     * @return ResponseEntity containing a message about the result.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    /**
     * Delete a task by ID.
     *
     * @param id The ID of the task to delete.
     * @return ResponseEntity containing a message about the result.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteTask(@PathVariable Integer id);

    /**
     * Delete a subtask by ID.
     *
     * @param id The ID of the subtask to delete.
     * @return ResponseEntity containing a message about the result.
     */
    @DeleteMapping(path = "/deleteSubTask/{id}")
    ResponseEntity<String> deleteSubTask(@PathVariable Integer id);
}
