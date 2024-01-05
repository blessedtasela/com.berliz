package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "trainer_pricing")
public class TrainerPricing implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(name = "price_online", columnDefinition = "DECIMAL(10, 2)")
    private BigDecimal priceOnline;

    @Column(name = "price_hybrid", columnDefinition = "DECIMAL(10, 2)")
    private BigDecimal priceHybrid;

    @Column(name = "price_personal", columnDefinition = "DECIMAL(10, 2)")
    private BigDecimal pricePersonal;

    @Column(name = "discount_3_months", columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal discount3Months;

    @Column(name = "discount_6_months", columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal discount6Months;

    @Column(name = "discount_9_months", columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal discount9Months;

    @Column(name = "discount_12_months", columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal discount12Months;

    @Column(name = "discount_2_programs", columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal discount2Programs;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "last_update", columnDefinition = "DATE")
    private Date lastUpdate;

}
