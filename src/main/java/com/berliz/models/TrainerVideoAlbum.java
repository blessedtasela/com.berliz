package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "trainerVideoAlbum")
public class TrainerVideoAlbum {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_fk", nullable = false)
    private Trainer trainer;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "video")
    private String video;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "last_update", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;

    @PrePersist
    public void prePersist() {
        this.uuid = generateUuid();
    }

    private String generateUuid() {
        String trainerName = this.trainer.getName();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String dateCreated = dateFormat.format(this.date);
        String id = String.valueOf(this.id);

        return trainerName + "_" + dateCreated + "_ID-" + id;
    }
}
