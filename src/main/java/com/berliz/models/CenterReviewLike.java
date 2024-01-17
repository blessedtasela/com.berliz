package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

@NamedQuery(name = "CenterReviewLike.findByCenterReviewId",
        query = "SELECT crl FROM CenterReviewLike crl WHERE crl.centerReview.id = :id")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "centerReviewLike")
public class CenterReviewLike {


    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_fk")
    private User user;

    @ManyToOne
    @JoinColumn(name = "centerReview_fk")
    private CenterReview centerReview;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

}
