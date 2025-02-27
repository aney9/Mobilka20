package com.example.pract20;

public class User {
    private String ID;
    private String role;
    private String email;

    public User(String ID, String role, String email){
        this.role = role;
        this.ID = ID;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return ID;
    }

    public String getRole() {
        return role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(String id) {
        this.ID = ID;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
