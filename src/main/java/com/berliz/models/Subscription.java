package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "Subscription.findActiveSubscriptionByUser",
        query = "select s from Subscription s where s.status='true' AND s.user = :user")

@NamedQuery(name = "Subscription.getActiveSubscriptions",
        query = "select s from Subscription s where s.status='true'")

@NamedQuery(name = "Subscription.countClientSubscriptionsByEmail",
        query = "SELECT COUNT(s) FROM Subscription s WHERE s.user.email =: email")

@NamedQuery(name = "Subscription.countMemberSubscriptionsByEmail",
        query = "SELECT COUNT(s) FROM Subscription s WHERE s.user.email =: email")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "subscription")
public class Subscription implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @ManyToOne
    @JoinColumn(name = "center_id")
    private Center center;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(name = "startDate", columnDefinition = "DATE")
    private Date startDate;

    @Column(name = "endDate", columnDefinition = "DATE")
    private Date endDate;

    @Column(name = "months")
    private Integer months;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}
