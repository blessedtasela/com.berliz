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
@Table(name = "review")
public class Review implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_entity_fk", nullable = false)
    private User entity;

    @Column(name = "review", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "photoBefore")
    private String photoBefore;

    @Column(name = "photoAfter")
    private String photoAfter;

    @Column(name = "likes", columnDefinition = "INTEGER")
    private Integer likes;

    @Column(name = "date", columnDefinition = "DATE")
    private String date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private String lastUpdate;
}
