package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "Newsletter.getAllNewsletters",
        query = "select n from Newsletter n")

@NamedQuery(name = "Newsletter.updateStatus",
        query = "update Newsletter n set n.status=:status where n.id=:id")

@NamedQuery(name = "Newsletter.updateNewsletter",
        query = "update Newsletter n set n.email =:email," +
        "n.lastUpdate = CURRENT_TIMESTAMP where n.id =:id")

@NamedQuery(name = "Newsletter.getActiveNewsletters",
        query = "select n from Newsletter n WHERE n.status ='true'")

@NamedQuery(name = "Newsletter.getAllActiveEMails",
        query = "select n.email from Newsletter n where n.status='true'")

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "newsletter")
public class Newsletter implements Serializable {

    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "email")
    private String email;

    @Column(name = "status")
    private String status;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @Column(name = "lastUpdate", columnDefinition = "TIMESTAMP")
    private Date lastUpdate;
}
