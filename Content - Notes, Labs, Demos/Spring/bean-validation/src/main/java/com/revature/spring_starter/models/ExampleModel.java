package com.revature.spring_starter.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Entity
public class ExampleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Name is required")
    @Size(max = 50)
    private String name;

    @NotNull
    @Email
    private String email;

    @Min(0) @Max(4)
    private double gpa;

    @Past
    private LocalDate birthDate;

    @Pattern(regexp = "\\d{3}-\\d{4}")
    private String phone;

}
