package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "Testimonial.getActiveTestimonials",
        query = "select t from Testimonial t where t.status='true'")

@NamedQuery(name = "Testimonial.countCenterTestimonialsByEmail",
        query = "SELECT COUNT(t) FROM Testimonial t WHERE t.center.partner.user.email =: email")

@NamedQuery(name = "Testimonial.countUserTestimonialsByEmail",
        query = "SELECT COUNT(t) FROM Testimonial t WHERE t.user.email =: email")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "testimonial")
public class Testimonial implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_fk", nullable = false)
    private Center center;

    @Column(name = "testimonial", columnDefinition = "TEXT")
    private String testimonial;

    @Column(name = "likes", columnDefinition = "INTEGER")
    private Integer likes;

    @Column(name = "status")
    private String status;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;
}
