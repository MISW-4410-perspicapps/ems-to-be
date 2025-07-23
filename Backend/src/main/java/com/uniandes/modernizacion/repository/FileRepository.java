/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.uniandes.modernizacion.repository;

import com.uniandes.modernizacion.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Andres Alarcon
 */
public interface FileRepository extends JpaRepository<File, Long> {
}
