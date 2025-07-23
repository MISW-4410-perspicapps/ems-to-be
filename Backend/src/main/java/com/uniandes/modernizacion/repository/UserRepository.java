/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.uniandes.modernizacion.repository;

import com.uniandes.modernizacion.model.Registration;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Andres Alarcon
 */
@Repository
public interface UserRepository extends JpaRepository<Registration, String> {

    Optional<Registration> findByUsername(String username);
}
