package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "centerLocation")
public class CenterLocation {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    @Column(name = "subName")
    private String subName;

    @Column(name = "locationUrl", columnDefinition = "TEXT")
    private String locationUrl;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "coverPhoto")
    private String coverPhoto;

    @Column(name = "ratings", columnDefinition = "FLOAT")
    private double ratings;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;
}
