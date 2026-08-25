package com.example.SPR_GCE_REQMAPPING;

/**
 * This class represents some data the server is keeping. In a RESTful server we would call this thing a "resource".
 * This is basically just a POJO, Model, or Entity. The contents of this file are unimportant for this coding example.
 */
public class Resource {
    private String firstName;
    private String lastName;
    private String email;

    public Resource(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Resource() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
