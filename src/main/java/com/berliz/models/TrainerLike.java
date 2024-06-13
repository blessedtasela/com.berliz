package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "TrainerLike.findByTrainerId",
        query = "SELECT tl FROM TrainerLike tl WHERE tl.trainer.id = :id")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "trainerLike")
public class TrainerLike implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_fk")
    private User user;

    @ManyToOne
    @JoinColumn(name = "trainer_fk")
    private Trainer trainer;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

}
