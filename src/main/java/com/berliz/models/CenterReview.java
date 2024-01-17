package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

@NamedQuery(name = "CenterReview.getActiveCenterReviewsByCenter",
        query = "SELECT cr FROM CenterReview cr WHERE cr.status = 'true' AND cr.center = :center")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "centerReview")
public class CenterReview {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_fk", nullable = false)
    private Center center;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_fk", nullable = false)
    private Member member;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "likes", columnDefinition = "INTEGER")
    private Integer likes;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "last_update", columnDefinition = "DATE")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;
}
