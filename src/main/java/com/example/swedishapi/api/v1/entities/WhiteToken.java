package com.example.swedishapi.api.v1.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "white_tokens")
public class WhiteToken{

    @Id
    @Column(name = "token")
    private String token;

    @Column(name = "refresh", unique = true)
    private String refresh;

    public WhiteToken(){
    }

    public WhiteToken(String token, String refresh) {
        this.token = token;
        this.refresh = refresh;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }
}