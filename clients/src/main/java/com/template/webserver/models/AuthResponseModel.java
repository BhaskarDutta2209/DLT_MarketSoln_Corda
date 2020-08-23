package com.template.webserver.models;

public class AuthResponseModel {

    private final String jwt;

    public AuthResponseModel(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }
}
