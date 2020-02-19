package com.example.swedishapi.api.v1.event_handlers;

import com.example.swedishapi.api.v1.entities.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@RepositoryEventHandler(User.class)
public class UserEventHandler {

    @Autowired 
    private BCryptPasswordEncoder passwordEncoder;

    @HandleBeforeCreate     
    public void handleUserCreate(User user) throws Exception {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
    }
}