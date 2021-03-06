package com.example.swedishapi.api.v1.repositories;

import com.example.swedishapi.api.v1.entities.Question;
import com.example.swedishapi.api.v1.entities.Test;
import com.example.swedishapi.api.v1.entities.User;
import com.example.swedishapi.api.v1.entities.Word;

import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ExposeIdsConfiguration implements RepositoryRestConfigurer {

  @Override
  public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
    config.exposeIdsFor(Word.class, Question.class, Test.class, User.class);
  }
}