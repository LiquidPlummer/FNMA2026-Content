package com.revature.SPR_GCE_ANNO_CONFIG;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class MyEntity {
    @Id
    private Integer id;

    @Column
    private String message;

    public MyEntity(Integer id, String message) {
        this.id = id;
        this.message = message;
    }

    public MyEntity(String message) {
        this.message = message;
    }

    public MyEntity() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
