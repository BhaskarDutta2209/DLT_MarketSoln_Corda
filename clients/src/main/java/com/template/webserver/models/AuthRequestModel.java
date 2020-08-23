package com.template.webserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthRequestModel {
    private final String username;
    private final String password;

    public AuthRequestModel(@JsonProperty("username") String username,
                            @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
