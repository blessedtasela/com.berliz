package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NamedQuery(name = "Client.getActiveClients",
        query = "select c from Client c where c.status='true'")

@NamedQuery(name = "Client.countTrainerClientsByEmail",
        query = "SELECT COUNT(c) FROM Client c JOIN c.subscriptions s " +
                "JOIN s.trainer t JOIN t.partner p JOIN p.user u WHERE u.email = :email")

@NamedQuery(name = "Client.findByUserId",
        query = "SELECT c FROM Client c WHERE c.user.id = :id")

@NamedQuery(name = "Client.getMyClientsByTrainer",
        query = "SELECT c FROM Client c JOIN c.subscriptions s WHERE s.trainer = :trainer")

@NamedQuery(name = "Client.getMyActiveClientsByTrainer",
        query = "SELECT c FROM Client c JOIN c.subscriptions s " +
                "WHERE s.trainer = :trainer AND s.status = 'active'")


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

    @Column(name = "height", columnDefinition = "FLOAT")
    private double height;

    @Column(name = "weight", columnDefinition = "FLOAT")
    private double weight;

    @Column(name = "bodyFat", columnDefinition = "FLOAT")
    private double bodyFat;

    @Column(name = "medicalConditions")
    private String medicalConditions;

    @Column(name = "dietaryPreference")
    private String dietaryPreferences;

    @Column(name = "dietaryRestrictions")
    private String dietaryRestrictions;

    @Column(name = "caloriesIntake", columnDefinition = "INTEGER")
    private Integer caloriesIntake;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "client_subscription",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "subscription_id")
    )
    private Set<Subscription> subscriptions = new HashSet<>();

    @Column(name = "mode")
    private String mode;

    @Column(name = "motivation")
    private String motivation;

    @Column(name = "targetWeight", columnDefinition = "FLOAT")
    private double targetWeight;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}
