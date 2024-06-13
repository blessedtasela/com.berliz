package com.berliz.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;


@NamedQuery(name = "Notification.bulkDeleteByIds",
        query = "DELETE FROM Notification WHERE id IN :ids")

@NamedQuery(name = "Notification.bulkReadByIds",
        query = "UPDATE Notification SET read = true WHERE id IN :ids")

@NamedQuery(name = "Notification.bulkUnreadByIds",
        query = "UPDATE Notification SET read = false WHERE id IN :ids")



@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "notification")
public class Notification {


    private static final long SerialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;

    @Column(name = "notification", columnDefinition = "TEXT")
    private String notification;

    @Column(name = "isRead", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean read;

    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private Date date;

    @PrePersist
    protected void onCreate() {
        this.date = new Date();
    }

}
