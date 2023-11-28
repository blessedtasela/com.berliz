package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.*;

@NamedQuery(name = "MuscleGroup.getActiveMuscleGroups",
        query = "select mg from MuscleGroup mg WHERE mg.status ='true'")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "muscleGroup")
public class MuscleGroup implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "bodyPart")
    private String bodyPart;

    @ManyToMany(mappedBy = "muscleGroups")
    private Set<Exercise> exercises = new HashSet<>();

    @Column(name = "image", columnDefinition = "BYTEA")
    private byte[] image;

    @Column(name = "status")
    private String status;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;
}
