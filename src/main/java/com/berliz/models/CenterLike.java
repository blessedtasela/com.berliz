package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "CenterLike.findByCenterId",
        query = "SELECT cl FROM CenterLike cl WHERE cl.center.id = :id")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "centerLike")
public class CenterLike implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_fk")
    private User user;

    @ManyToOne
    @JoinColumn(name = "center_fk")
    private Center center;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

}
