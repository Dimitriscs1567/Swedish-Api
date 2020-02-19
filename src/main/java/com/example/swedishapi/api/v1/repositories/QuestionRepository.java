package com.example.swedishapi.api.v1.repositories;

import com.example.swedishapi.api.v1.entities.Question;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends CrudRepository<Question, Long>{

}