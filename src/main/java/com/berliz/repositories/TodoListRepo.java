package com.berliz.repositories;

import com.berliz.models.TodoList;
import com.berliz.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoListRepo extends JpaRepository<TodoList, Integer> {

    TodoList findByTask(@Param("task") String name);

    List<TodoList> findByUser(User user);

    Boolean existsByUserAndTask(User user, String task);

    Integer countMyTodosByEmail(@Param("email") String email);

}
