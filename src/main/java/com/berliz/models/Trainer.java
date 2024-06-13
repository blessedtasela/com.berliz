package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

// Named queries are used to define reusable queries
@NamedQuery(name = "Trainer.updateStatus", query = "update Trainer t " +
        "set t.status=:status where t.id=:id")

@NamedQuery(name = "Trainer.updatePartnerId", query = "update Trainer t " +
        "set t.partner.id=:partnerId where t.id=:id")

@NamedQuery(name = "Trainer.findByTrainerId",
        query = "SELECT t FROM Trainer t WHERE t.id = :id")

@NamedQuery(name = "Trainer.findByPartnerId",
        query = "SELECT t FROM Trainer t WHERE t.partner.id = :id")

@NamedQuery(name = "Trainer.findByUserId",
        query = "SELECT t FROM Trainer t WHERE t.partner.user.id = :id")

@NamedQuery(name = "Trainer.countTrainersByUserId",
        query =  "SELECT COUNT(t) FROM Trainer t WHERE t.partner.user.id = :id")

@NamedQuery(name = "Trainer.getActiveTrainers",
        query = "SELECT t FROM Trainer t WHERE t.status = 'true'")


@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "trainer")
public class Trainer implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_fk", nullable = false)
    private Partner partner;

    @Column(name = "name")
    private String name;

    @Column(name = "motto", columnDefinition = "TEXT")
    private String motto;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "experience")
    private String experience;

    @Column(name = "photo", columnDefinition = "BYTEA")
    private byte[] photo;

    @Column(name = "likes", columnDefinition = "INTEGER")
    private int likes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "trainer_category",
            joinColumns = @JoinColumn(name = "trainer_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categorySet = new HashSet<>();

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}
