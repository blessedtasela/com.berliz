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
@Table(name = "trainerIntroduction")
public class TrainerIntroduction {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_fk", nullable = false)
    private Trainer trainer;

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "coverPhoto")
    private byte [] coverPhoto;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "last_update", columnDefinition = "DATE")
    private Date lastUpdate;
}
