package com.berliz.models;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "Brand.updateStatus", query = "update Brand b set b.status=:status where b.id=:id")

@NamedQuery(name = "Brand.getAllBrands", query = "select b from Brand b")

@NamedQuery(name = "Brand.updateBrand", query = "update Brand b set b.name =:name, b.description =:description, " +
        "b.lastUpdate = CURRENT_TIMESTAMP, b.ratings=: ratings where b.id =: id")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "brand")
public class Brand implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ratings", columnDefinition = "FLOAT")
    private double ratings;

    @Column(name = "likes", columnDefinition = "INTEGER")
    private int likes;

    @Column(name = "status")
    private String  status;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;
}
