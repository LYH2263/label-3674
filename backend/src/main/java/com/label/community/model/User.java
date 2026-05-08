package com.label.community.model;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String role;
    private String fullName;
    private String phone;
    private String email;
    private boolean realNameVerified;
    private String idCardEnc;
    private String status;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isRealNameVerified() {
        return realNameVerified;
    }

    public void setRealNameVerified(boolean realNameVerified) {
        this.realNameVerified = realNameVerified;
    }

    public String getIdCardEnc() {
        return idCardEnc;
    }

    public void setIdCardEnc(String idCardEnc) {
        this.idCardEnc = idCardEnc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
