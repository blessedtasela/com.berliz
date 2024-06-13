package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "TodoList.countMyTodosByEmail",
        query = "SELECT COUNT(tl) FROM TodoList tl WHERE tl.user.email =: email")

@NamedQuery(name = "TodoList.bulkUpdateStatusByIds",
        query = "UPDATE TodoList SET status = :status WHERE id IN :ids")

@NamedQuery(name = "TodoList.bulkDeleteByIds",
        query = "DELETE FROM TodoList WHERE id IN :ids")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "todoList")
public class TodoList implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "task", columnDefinition = "TEXT")
    private String task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}

