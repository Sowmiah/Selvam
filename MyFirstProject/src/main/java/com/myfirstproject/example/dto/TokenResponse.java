package com.myfirstproject.example.dto;

public class TokenResponse {
    private String token;
    private String client;
    private String stat;
    private String emsg;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getStat() {
        return stat;
    }

    public void setStatus(String stat) {
        this.stat = stat;
    }

    public String getEmsg() {
        return emsg;
    }

    public void setEmsg(String emsg) {
        this.emsg = emsg;
    }
}
