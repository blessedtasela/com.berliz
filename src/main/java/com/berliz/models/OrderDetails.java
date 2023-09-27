package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@NamedQuery(name = "OrderDetails.findByOrderDetailsId",
        query = "SELECT od FROM OrderDetails od WHERE od.id = :orderDetailsId")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "orderDetails")
public class OrderDetails implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_fk", nullable = false)
    private Product product;

    @Column(name = "quantity", columnDefinition = "INTEGER")
    Integer quantity;

    @Column(name = "subTotal", columnDefinition = "FLOAT")
    double subTotal;

}
