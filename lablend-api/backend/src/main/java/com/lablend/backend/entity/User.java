package com.lablend.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing a user in the system.
 * @version 1.0
 */

@Entity
@Table(name = "users")
public class User {

    /** Unique ID of a user */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Name of a user */
    private String name;

    /** Email of a user */
    private String email;

    /** Password of a user */
    private String password;

    /** Role of a user */
    @Enumerated(EnumType.STRING)
    private UserRole role;

    /** Default constructor */
    public User() {
    }

    /** Constructor for creating a user with specified attributes 
     * @param name  the name of the user
     * @param email the email of the user
     * @param password the password of the user
     * @param role  the role of the user (e.g., ADMIN, USER)
    */
    public User(String name, String email, String password, UserRole role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    /** @return unique ID of a user */
    public Long getId() {
        return id;
    }

    /** Setter for the unique ID of a user */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return the name of a user */
    public String getName() {
        return name;
    }

    /** Setter for the name of a user */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the email of a user */
    public String getEmail() {
        return email;
    }

    /** Setter for the email of a user */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return the password of a user */
    public String getPassword() {
        return password;
    }

    /** Setter for the password of a user */
    public void setPassword(String password) {
        this.password = password;
    }

    /** @return the role of a user */
    public UserRole getRole() {
        return role;
    }

    /** Setter for the role of a user */
    public void setRole(UserRole role) {
        this.role = role;
    }
}