package com.example.swedishapi.api.v1.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "white_tokens")
public class WhiteToken{

    @Id
    @Column(name = "token", unique = true)
    private String token;

    public WhiteToken() {
    }

    public WhiteToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}