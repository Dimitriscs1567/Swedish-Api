package com.example.swedishapi.api.v1.repositories;

import java.util.Optional;

import com.example.swedishapi.api.v1.entities.WhiteToken;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WhiteTokenRepository extends CrudRepository<WhiteToken, String>{

    Optional<WhiteToken> findByRefresh(String refresh);
    
}