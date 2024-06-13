package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;
import java.util.List;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "trainerBenefit")
public class TrainerBenefit {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_fk", nullable = false, unique = true)
    private Trainer trainer;

    @ElementCollection
    @CollectionTable(name = "benefits", joinColumns = @JoinColumn(name = "trainer_benefit_id"))
    @Column(name = "benefits")
    private List<String> benefits;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "last_update", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;
}
