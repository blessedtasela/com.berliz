package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "ClientReviewLike.findByClientReviewId",
        query = "SELECT crl FROM ClientReviewLike crl WHERE crl.clientReview.id = :id")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "trainerClientReviewLike")
public class ClientReviewLike implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_fk")
    private User user;

    @ManyToOne
    @JoinColumn(name = "clientReview_fk")
    private ClientReview clientReview;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

}
