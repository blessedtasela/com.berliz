package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "Payment.findActivePaymentByUser",
        query = "select p from Payment p where p.status='true' AND p.user = :user")

@NamedQuery(name = "Payment.getActivePayments", query = "select p from Payment p where p.status='true'")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "payment")
public class Payment implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @ManyToOne
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "amount", columnDefinition = "FLOAT")
    private double amount;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;
}
