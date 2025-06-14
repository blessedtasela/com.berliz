package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(
        name = "Photo.getPhotosByTrainerPhotoAlbum",
        query = "SELECT p FROM Photo p WHERE p.ownerType = 'trainerPhotoAlbum' AND p.ownerId = :ownerId"
)

@NamedQuery(
        name = "Photo.getAllTrainerPhotoAlbumPhotos",
        query = "SELECT p FROM Photo p WHERE p.ownerType = 'trainerPhotoAlbum'"
)


@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "photo")
public class Photo implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

    @Column(name = "strapi_id", nullable = false, columnDefinition = "INTEGER")
    private Integer strapiId;

    @Column(name = "caption", columnDefinition = "TEXT")
    private String caption;

    @Column(name = "name", columnDefinition = "name")
    private String name;

    @Column(name = "mimeType", columnDefinition = "TEXT")
    private String mimeType;

    @Column(name = "byteSize", columnDefinition = "INTEGER")
    private Long byteSize;

    @Column(name = "owner_type", nullable = false)
    private String ownerType;  // e.g. "trainer", "center", "album"

    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;  // ID of the trainer, center, or album

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;
}
