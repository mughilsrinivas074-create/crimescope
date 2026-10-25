package com.crimescope.web.dto;

import java.util.List;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;

    public AuthResponse(String token, String username, String email, String fullName, List<String> roles) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getRoles() {
        return roles;
    }
}
