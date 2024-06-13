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
@NamedQuery(name = "Order.updateStatus",
        query = "update Order o set o.status=:status where o.id=:id")

@NamedQuery(name = "Order.findByUserId",
        query = "SELECT o FROM Order o WHERE o.user.id = :id")

@NamedQuery(name = "Order.findByStatus",
        query = "SELECT o FROM Order o WHERE o.status = :status")

@NamedQuery(name = "Order.findByOrderId",
        query = "SELECT o FROM Order o WHERE o.id = :id")

@NamedQuery(name = "Order.countOrdersByEmail",
       query =  "SELECT COUNT(o) FROM Order o WHERE o.user.email = :email")

// The @Data annotation generates all the boilerplate that is normally associated with simple POJOs
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "orders")
public class Order implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "uuid")
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "order_orderDetails",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "orderDetails_id")
    )
    private Set<OrderDetails> orderDetailsSet = new HashSet<>();

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "contact")
    private String contact;

    @Column(name = "country")
    private String country;

    @Column(name = "state")
    private String state;

    @Column(name = "city")
    private String city;

    @Column(name = "postalCode", columnDefinition = "INTEGER")
    private Integer postalCode;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "paymentMethod")
    private String paymentMethod;

    @Column(name = "totalAmount", columnDefinition = "FLOAT")
    private double totalAmount;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;
}
