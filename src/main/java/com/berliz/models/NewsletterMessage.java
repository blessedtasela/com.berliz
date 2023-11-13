package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "newsletterMessage")
public class NewsletterMessage implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "subject")
    private String subject;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "date")
    private Date date;
}