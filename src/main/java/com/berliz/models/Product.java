package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NamedQuery(name = "Product.updateStatus", query = "update Product p " +
        "set p.status=:status where p.id=:id")

@NamedQuery(name = "Product.findByStoreId",
        query = "SELECT p FROM Product p WHERE p.store.id = :id")

@NamedQuery(name = "Product.findByBrandId",
        query = "SELECT p FROM Product p WHERE p.brand.id = :id")

@NamedQuery(name = "Product.findByProductId",
        query = "SELECT p FROM Product p WHERE p.id = :id")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "product")
public class Product implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_fk", nullable = false)
    private Store store;

    @Column(name = "classify")
    private String classify;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sex")
    private String sex;

    @Column(name = "likes", columnDefinition = "INTEGER")
    private int likes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categorySet = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_fk", nullable = false)
    private Brand brand;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_tag",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tagSet = new HashSet<>();

    @Column(name = "price", columnDefinition = "FLOAT")
    private double price;

    @Column(name = "quantity", columnDefinition = "INTEGER")
    private Integer quantity;

    @Column(name = "photo")
    private String photo;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}
