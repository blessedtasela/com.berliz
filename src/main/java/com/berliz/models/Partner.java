package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NamedQuery(name = "Partner.updateStatus", query = "update Partner p " +
        "set p.status=:status where p.id=:id")

@NamedQuery(name = "Partner.updateUserId", query = "update Partner p " +
        "set p.user.id=:userId where p.id=:id")

@NamedQuery(name = "Partner.findByUserId",
        query = "SELECT p FROM Partner p WHERE p.user.id = :id")

@NamedQuery(name = "Partner.findByPartnerId",
        query = "SELECT p FROM Partner p WHERE p.id = :id")

@NamedQuery(name = "Partner.countPartnerByEmail",
        query = "SELECT COUNT(p) FROM Partner p WHERE p.user.email =: email")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "partner")
public class Partner implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;

    @Column(name = "certificate", columnDefinition = "BYTEA")
    private byte[] certificate;

    @Column(name = "motivation", columnDefinition = "TEXT")
    private String motivation;

    @Column(name = "cv", columnDefinition = "BYTEA")
    private byte[] cv;

    @Column(name = "facebookUrl")
    private String facebookUrl;

    @Column(name = "instagramUrl")
    private String instagramUrl;

    @Column(name = "youtubeUrl")
    private String youtubeUrl;

    @Column(name = "role")
    private String role;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}
