package com.berliz.repository;

import com.berliz.models.Tag;
import com.berliz.models.TodoList;
import com.berliz.models.Trainer;
import com.berliz.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface TodoListRepo extends JpaRepository<TodoList, Integer> {

    TodoList findByTask(@Param("task") String name);

    TodoList findByUser(User user);

    TodoList existsByUserAndTask(User user, String task);

}
