package com.example.swedishapi.api.v1.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.example.swedishapi.api.v1.entities.Test;
import com.example.swedishapi.api.v1.entities.User;
import com.example.swedishapi.api.v1.repositories.TestRepository;
import com.example.swedishapi.api.v1.repositories.UserRepository;
import com.example.swedishapi.api.v1.security.JwtTokenProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController{

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TestRepository testRepository;

    @GetMapping("/tests")
    public ResponseEntity getTests(HttpServletRequest request){
        String token = jwtTokenProvider.resolveToken(request);

        User user = userRepository.findByEmail(jwtTokenProvider.getEmail(token)).get();
        List<Map<String, Object>> result = new ArrayList<>();

        if(user.getRoles().get(0).equals("ROLE_ADMIN")){
            testRepository.findAll().forEach(test -> {
                result.add(test.toMap());
            });
        }
        else{
            result.addAll(user.getTests().stream()
                .map(test -> test.toMap())
                .collect(Collectors.toList()));
        }

        return new ResponseEntity(result, HttpStatus.OK);
    }

    @GetMapping("/tests/{id}")
    public ResponseEntity getTest(HttpServletRequest request, @PathVariable Long id){
        String token = jwtTokenProvider.resolveToken(request);

        User user = userRepository.findByEmail(jwtTokenProvider.getEmail(token)).get();

        if(!testRepository.findById(id).isPresent()){
            return new ResponseEntity(
                "Error: Test with id: " + id + " could not be found.", 
                HttpStatus.BAD_REQUEST);
        }

        Test test = testRepository.findById(id).get();

        if(user.getRoles().get(0).equals("ROLE_ADMIN")){
            return new ResponseEntity(test.toMap(), HttpStatus.OK);
        }
        else{
            boolean permits = user.getTests().stream()
                .anyMatch(testEntity -> {
                    return testEntity.getId() == id;
                });

            if(permits){
                return new ResponseEntity(test.toMap(), HttpStatus.OK);
            }
            else{
                return new ResponseEntity(HttpStatus.UNAUTHORIZED);
            }
        }
    }
}