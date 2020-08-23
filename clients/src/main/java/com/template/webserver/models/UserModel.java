package com.template.webserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserModel {
    private final String userName;

    public UserModel(@JsonProperty("userName") String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
