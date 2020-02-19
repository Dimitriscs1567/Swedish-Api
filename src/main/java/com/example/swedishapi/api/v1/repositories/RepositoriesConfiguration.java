package com.example.swedishapi.api.v1.repositories;

import com.example.swedishapi.api.v1.event_handlers.UserEventHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoriesConfiguration{
     
    public RepositoriesConfiguration(){
        super();
    }

    @Bean
    UserEventHandler userEventHandler() {
        return new UserEventHandler();
    }
}