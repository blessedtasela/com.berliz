package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "CategoryLike.findByCategoryId",
        query = "SELECT cl FROM CategoryLike cl WHERE cl.category.id = :id")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "categoryLike")
public class CategoryLike implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_fk")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_fk")
    private Category category;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

}
