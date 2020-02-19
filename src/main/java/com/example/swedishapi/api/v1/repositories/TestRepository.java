package com.example.swedishapi.api.v1.repositories;

import com.example.swedishapi.api.v1.entities.Test;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends CrudRepository<Test, Long>{

}