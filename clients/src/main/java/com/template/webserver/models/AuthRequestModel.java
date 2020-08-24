package com.template.webserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthRequestModel {
    private final String username;

    public AuthRequestModel(@JsonProperty("username") String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
