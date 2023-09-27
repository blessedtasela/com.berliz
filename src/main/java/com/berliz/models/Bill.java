package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "Bill.findByBillId",
        query = "SELECT b FROM Bill b WHERE b.id = :id")

@NamedQuery(name = "Bill.findByOrderId",
        query = "SELECT b FROM Bill b WHERE b.order.id = :id")

@NamedQuery(name = "Bill.findByUserId",
        query = "SELECT b FROM Bill b WHERE b.user.id = :id")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "bill")
public class Bill implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_fk")
    private Order order;

    @Column(name = "uuid")
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastPrinted", columnDefinition = "DATE")
    private Date lastPrinted;
}
