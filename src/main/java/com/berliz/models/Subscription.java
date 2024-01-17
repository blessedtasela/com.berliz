package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @ManyToOne
    @JoinColumn(name = "center_id")
    private Center center;

    @Column(name = "startDate", columnDefinition = "DATE")
    private Date startDate;

    @Column(name = "endDate", columnDefinition = "DATE")
    private Date endDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "client_category",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @Column(name = "months")
    private Integer months;

    @Column(name = "amount", columnDefinition = "DECIMAL(10, 2)")
    private BigDecimal amount;

    @Column(name = "mode")
    private String mode;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}
