package com.ecommerce.webapp.ecommerce.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String mobileNumber;
    private String address;
    private String email;
    private String city;
    private String state;
    private String pinCode;
    private String password;
    private String profileImage;
    private String role;
    private boolean isEnable;
    private boolean accountNonLocked;
    private Integer failedAttempts;
    private Date lockDate;
    private String resetToken;
}
