package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

// Named queries are used to define reusable queries
@NamedQuery(name = "Center.updateStatus", query = "update Center c " +
        "set c.status=:status where c.id=:id")

@NamedQuery(name = "Center.updatePartnerId", query = "update Center c " +
        "set c.partner.id=:newId where c.id=:id")

@NamedQuery(name = "Center.findByCenterId",
        query = "SELECT c FROM Center c WHERE c.id = :id")

@NamedQuery(name = "Center.findByPartnerId",
        query = "SELECT c FROM Center c WHERE c.partner.id = :id")


@NamedQuery(name = "Center.getByCategoryId",
        query = "select ct from Center ct " +
                "join ct.categorySet c " +
                "where c.id = :id")

// The @Data annotation generates all the boilerplate that is normally associated with simple POJOs
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "center")
public class Center implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_fk", nullable = false)
    private Partner partner;

    @Column(name = "name")
    private String name;

    @Column(name = "motto")
    private String motto;

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "address")
    private String address;

    @Column(name = "experience", columnDefinition = "TEXT")
    private String experience;

    @Column(name = "location")
    private String location;

    @Column(name = "photo")
    private String photo;

    @Column(name = "likes", columnDefinition = "INTEGER")
    private int likes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "center_category",
            joinColumns = @JoinColumn(name = "center_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categorySet = new HashSet<>();

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}
