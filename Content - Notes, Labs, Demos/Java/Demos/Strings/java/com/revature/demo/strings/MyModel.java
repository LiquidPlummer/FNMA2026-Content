package com.revature.demo.strings;

import java.util.Objects;

public class MyModel {
    String username;
    String password;

    public MyModel(String username, String password) {
        this.username = username;
        this.password = password;
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

    @Override
    public boolean equals(Object that) {
        if (that == null || this.getClass() != that.getClass()) return false;
        MyModel myModel = (MyModel) that;//type cast
        return Objects.equals(username, myModel.username) && Objects.equals(password, myModel.password);
    }


}
