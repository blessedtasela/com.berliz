package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "comment")
public class Comment implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_entity_fk", nullable = false)
    private User entity;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "likes", columnDefinition = "INTEGER")
    private Integer likes;

    @Column(name = "ratings", columnDefinition = "FLOAT")
    private double ratings;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private String date;

    @Column(name = "lastUpdate", columnDefinition = "TIMESTAMP")
    private String lastUpdate;

}
