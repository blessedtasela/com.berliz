package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "client")
public class Client implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;

    @Column(name = "height")
    private Double height;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "bodyFat")
    private Double bodyFat;

    @Column(name = "medicalConditions")
    private String medicalConditions;

    @Column(name = "dietaryPreference")
    private String dietaryPreferences;

    @Column(name = "dietaryRestrictions")
    private String dietaryRestrictions;

    @Column(name = "calorie_intake")
    private Integer calorieIntake;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_fk")
    private Trainer trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_fk")
    private Center center;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "client_category",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categorySet = new HashSet<>();

    @Column(name = "plan")
    private String plan;

    @Column(name = "mode")
    private String mode;

    @Column(name = "motivation")
    private String motivation;

    @Column(name = "targetWeight")
    private String targetWeight;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}
