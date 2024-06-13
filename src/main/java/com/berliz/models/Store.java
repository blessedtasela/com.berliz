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
@NamedQuery(name = "Store.updateStatus", query = "update Store s " +
        "set s.status=:status where s.id=:id")

@NamedQuery(name = "Store.updatePartnerId", query = "update Store s " +
        "set s.partner.id=:partnerId where s.id=:id")

@NamedQuery(name = "Store.findByStoreId",
        query = "SELECT s FROM Store s WHERE s.id = :id")

@NamedQuery(name = "Store.findByPartnerId",
        query = "SELECT s FROM Store s WHERE s.partner.id = :id")


@NamedQuery(name = "Store.getByCategoryId",
        query = "select s from Store s " +
                "join s.categorySet c " +
                "where c.id = :id")

// The @Data annotation generates all the boilerplate that is normally associated with simple POJOs
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "store")
public class Store implements Serializable {

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

    @Column(name = "address")
    private String address;

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "location")
    private String location;

    @Column(name = "photo")
    private String photo;

    @Column(name = "likes", columnDefinition = "INTEGER")
    private int likes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "store_category",
            joinColumns = @JoinColumn(name = "store_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categorySet = new HashSet<>();

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}
