package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

@NamedQuery(name = "CenterAnnouncement.getActiveCenterAnnouncements",
        query = "select ca from CenterAnnouncement ca where ca.status='true' AND ca.center = :center")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "centerAnnouncement")
public class CenterAnnouncement {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    @Column(name = "announcement", columnDefinition = "TEXT")
    private String announcement;

    @Column(name = "icon", columnDefinition = "BYTEA")
    private byte[] icon;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;
}
