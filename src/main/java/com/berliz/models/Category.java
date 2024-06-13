package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NamedQuery(name = "Category.updateStatus", query = "update Category c set c.status=:status where c.id=:id")

@NamedQuery(name = "Category.getActiveCategories", query = "select c from Category c where c.status='true'")

@NamedQuery(name = "Category.updateCategory", query = "update Category c set c.name =:name, " +
        "c.description =:description, c.lastUpdate =CURRENT_TIMESTAMP where c.id =: id")

@NamedQuery(name = "Category.getByTag",
        query = "select c from Category c " +
                "join c.tagSet t " +
                "where t.id = :id")

@NamedQuery(name = "Category.countByName",
        query = "SELECT COUNT(c) FROM Category c WHERE c.name = :name")

@NamedQuery(name = "Category.countByNameExceptId",
        query = "SELECT COUNT(c) FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.id <> :id")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "category")
public class Category implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "likes", columnDefinition = "INTEGER")
    private Integer likes;

    @Column(name = "photo")
    private String photo;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "category_tag",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tagSet = new HashSet<>();

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}
