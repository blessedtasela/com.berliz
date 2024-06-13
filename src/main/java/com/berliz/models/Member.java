package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NamedQuery(name = "Member.findByUserId",
        query = "SELECT m FROM Member m WHERE m.user.id = :id")

@NamedQuery(name = "Member.findByMemberId",
        query = "SELECT m FROM Member m WHERE m.id = :id")

@NamedQuery(name = "Member.getActiveMembers", query = "select m from Member m where m.status='true'")

@NamedQuery(name = "Member.countCenterMembersByEmail",
        query = "SELECT COUNT(m) FROM Member m JOIN m.subscriptions s " +
                "JOIN s.center c JOIN c.partner p JOIN p.user u WHERE u.email = :email")

@NamedQuery(name = "Member.getMyMembersByCenter",
        query = "SELECT m FROM Member m JOIN m.subscriptions s WHERE s.center = :center")

@NamedQuery(name = "Member.getMyActiveMembersByCenter",
        query = "SELECT m FROM Member m JOIN m.subscriptions s " +
                "WHERE s.center = :center AND s.status = 'active'")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "member")
public class Member implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;

    @Column(name = "height")
    private Double height;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "bodyFat")
    private Double bodyFat;

    @Column(name = "medicalConditions")
    private String medicalConditions;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "member_category",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "member_subscription",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "subscription_id")
    )
    private Set<Subscription> subscriptions = new HashSet<>();

    @Column(name = "motivation")
    private String motivation;

    @Column(name = "targetWeight")
    private double targetWeight;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}
