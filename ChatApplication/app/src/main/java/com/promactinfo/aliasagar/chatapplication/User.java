package com.promactinfo.aliasagar.chatapplication;

import com.google.gson.annotations.Expose;

/**
 * Created by Aliasagar on 08-03-2019.
 */

public class User {
    private int id;
    private String name;
    private String token;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
