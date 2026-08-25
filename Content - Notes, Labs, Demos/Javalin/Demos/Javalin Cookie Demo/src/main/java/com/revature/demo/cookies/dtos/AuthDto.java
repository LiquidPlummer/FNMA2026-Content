package com.revature.demo.cookies.dtos;

public class AuthDto {
    private String username;
    private String password;

    public AuthDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public AuthDto() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
