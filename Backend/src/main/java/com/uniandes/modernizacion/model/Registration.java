/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.uniandes.modernizacion.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private String username;
    private String password;
    private String firstname;
    private String role;
    private String id;    
    private Boolean activityStatus;
}
