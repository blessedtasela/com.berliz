package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

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

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}

