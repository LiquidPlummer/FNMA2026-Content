package com.revature.demo.cookies.services;

import com.revature.demo.cookies.dtos.RegistrationDto;

import java.util.HashMap;
import java.util.Map;

public class PersistenceService {
    Map<String, RegistrationDto> fakeDb = new HashMap<>();

    public void saveOrUpdate(RegistrationDto registrationDto) {
        fakeDb.put(registrationDto.getUsername(), registrationDto);
    }

    public RegistrationDto findByUsername(String username) {
        return fakeDb.get(username);
    }
}
