package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "Driver.updateStatus", query = "update Driver d " +
        "set d.status=:status where d.id=:id")

@NamedQuery(name = "Driver.updatePartnerId", query = "update Driver d " +
        "set d.partner.id=:newId where d.id=:id")

@NamedQuery(name = "Driver.findByDriverId",
        query = "SELECT d FROM Driver d WHERE d.id = :id")

@NamedQuery(name = "Driver.findByPartnerId",
        query = "SELECT d FROM Driver d WHERE d.partner.id = :id")


@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "driver")
public class Driver implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_fk", nullable = false)
    private Partner partner;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "likes", columnDefinition = "INTEGER")
    private int likes;

    @Column(name = "location")
    private String location;

    @Column(name = "vehicleType")
    private String vehicleType;

    @Column(name = "vehicleModel")
    private String vehicleModel;

    @Column(name = "licensePlate")
    private String licensePlate;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;

    @Column(name = "status")
    private String status;

}
