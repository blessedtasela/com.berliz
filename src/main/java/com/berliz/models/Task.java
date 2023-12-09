package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NamedQuery(name = "Task.findActiveTaskByUser",
        query = "select t from Task t where t.status='true' AND t.user = :user")

@NamedQuery(name = "Task.getActiveTasks",
        query = "select t from Task t WHERE t.status ='true'")

@NamedQuery(name = "Task.countClientTasksByEmail",
        query = "SELECT COUNT(t) FROM Task t WHERE t.user.email =: email")

@NamedQuery(name = "Task.countTrainerTasksByEmail",
        query = "SELECT COUNT(t) FROM Task t WHERE t.trainer.partner.user.email =: email")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "task")
public class Task implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "description")
    private String description;

    @Column(name = "priority")
    private String priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_fk", nullable = false)
    private Trainer trainer;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubTask> subTasks = new ArrayList<>();

    @Column(name = "startData", columnDefinition = "DATE")
    private Date startDate;

    @Column(name = "endDate", columnDefinition = "DATE")
    private Date endDate;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;
}
