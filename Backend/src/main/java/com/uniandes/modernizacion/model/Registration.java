/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.uniandes.modernizacion.model;

import java.util.Date;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

/**
 *
 * @author Andres Alarcon
 */
@Entity
@Table(name = "registration")
@Data
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String address;
    private String phone;
    private String username;
    private String password;
    
    @ManyToOne
    @JoinColumn(name = "role", referencedColumnName = "id")
    private Role role;
    
    @Column(name = "managerstatus")
    private Boolean managerStatus;
    
    @Column(name = "managerId")
    private Integer managerId;
    
    @Column(name = "activitystatus")
    private Boolean activityStatus;
    
    @Temporal(TemporalType.DATE)
    private Date date;
}
