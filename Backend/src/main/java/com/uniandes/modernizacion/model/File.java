/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.uniandes.modernizacion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import lombok.Data;

/**
 *
 * @author Andres Alarcon
 */
@Entity
@Table(name = "files")
@Data
public class File {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "url")
    private String url;
}
