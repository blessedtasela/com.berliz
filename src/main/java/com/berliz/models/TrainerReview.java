package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

@NamedQuery(name = "TrainerReview.getActiveTrainerReviewsByTrainer",
        query = "SELECT tr FROM TrainerReview tr WHERE tr.status = 'true' AND tr.trainer = :trainer")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "trainerReview")
public class TrainerReview {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_fk", nullable = false)
    private Trainer trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_fk", nullable = false)
    private Client client;

    @Column(name = "frontBefore")
    private String frontBefore;

    @Column(name = "frontAfter")
    private String frontAfter;

    @Column(name = "sideBefore")
    private String sideBefore;

    @Column(name = "sideAfter")
    private String sideAfter;

    @Column(name = "backBefore")
    private String backBefore;

    @Column(name = "backAfter")
    private String backAfter;

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;

    @Column(name = "likes", columnDefinition = "INTEGER")
    private Integer likes;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "last_update", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;
}
