package com.berliz.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class UserWrapper {

    private Integer id;

    private String firstname;

    private String lastname;

    private String phone;

    private String dob;

    private String country;

    private String state;

    private String city;

    private Integer postalCode;

    private String address;

    private String email;

    private Date date;

    private String status;

    private Date lastUpdate;

    public UserWrapper(Integer id, String firstname, String lastname, String phone,
                       String dob, String country, String state, String city,
                       String address, Integer postalCode,
                       String email, Date date, String status, Date lastUpdate) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phone = phone;
        this.dob = dob;
        this.country = country;
        this.state = state;
        this.city = city;
        this.address = address;
        this.postalCode = postalCode;
        this.email = email;
        this.date = date;
        this.lastUpdate = lastUpdate;
        this.status = status;
    }
}
