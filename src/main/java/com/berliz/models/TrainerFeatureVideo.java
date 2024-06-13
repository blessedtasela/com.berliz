package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "trainerFeatureVideo")
public class TrainerFeatureVideo {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_fk", nullable = false)
    private Trainer trainer;

    @Column(name = "video")
    private String video;

    @Column(name = "motivation", columnDefinition = "TEXT")
    private String motivation;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "last_update", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;
}
