package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "TrainerReviewLike.findByTrainerReviewId",
        query = "SELECT trl FROM TrainerReviewLike trl WHERE trl.trainerReview.id = :id")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "trainerReviewLike")
public class TrainerReviewLike implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_fk")
    private User user;

    @ManyToOne
    @JoinColumn(name = "trainerReview_fk")
    private TrainerReview trainerReview;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

}
