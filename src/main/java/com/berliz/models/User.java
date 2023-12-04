package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NamedQuery(name = "User.findByEmailId", query = "select u from User u where u.email =: email")

@NamedQuery(name = "User.findByUserId", query = "select u from User u where u.id =: id")

@NamedQuery(name = "User.getAllUsers", query = "select new com.berliz.wrapper.UserWrapper(u.id, u.firstname, " +
        "u.lastname, u.phone, u.dob, u.country, u.state, u.city, u.address, u.postalCode, u.email," +
        " u.date, u.status, u.lastUpdate) from User u where u.role='user'")

@NamedQuery(name = "User.updateStatus", query = "update User u set u.status=:status where u.id=:id")

@NamedQuery(name = "User.getAllAdminsMail", query = "select u.email from User u where u.role='admin'")

@NamedQuery(name = "User.updateUserRole", query = "update User u set u.role=:role where u.id=:id")

@NamedQuery(name = "User.getActiveUsers", query = "select u from User u where u.status='true'")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "users")
public class User implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "phone")
    private String phone;

    @Column(name = "dob")
    private String dob;

    @Column(name = "gender")
    private String gender;

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

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

    @Column(name = "token")
    private String token;

    @Column(name = "profilePhoto", columnDefinition = "BYTEA")
    private byte[] profilePhoto;

    @ManyToMany
    @JoinTable(
            name = "userCategoryLikes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> likedCategoriesSet = new HashSet<>();

    @Column(name = "status")
    private String status;

    @Column(name = "date", columnDefinition = "DATE")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "DATE")
    private Date lastUpdate;

}
