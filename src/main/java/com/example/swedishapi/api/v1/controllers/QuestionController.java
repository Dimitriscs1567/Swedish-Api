package com.example.swedishapi.api.v1.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.example.swedishapi.api.v1.entities.Question;
import com.example.swedishapi.api.v1.entities.User;
import com.example.swedishapi.api.v1.repositories.QuestionRepository;
import com.example.swedishapi.api.v1.repositories.UserRepository;
import com.example.swedishapi.api.v1.security.JwtTokenProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuestionController{

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    QuestionRepository questionRepository;

    @GetMapping("/questions")
    public ResponseEntity getQuestions(HttpServletRequest request){
        String token = jwtTokenProvider.resolveToken(request);

        User user = userRepository.findByEmail(jwtTokenProvider.getEmail(token)).get();
        List<Map<String, Object>> result = new ArrayList<>();

        if(user.getRoles().get(0).equals("ROLE_ADMIN")){
            questionRepository.findAll().forEach(question -> {
                result.add(question.toMap());
            });
        }
        else{
            result.addAll(user.getTests().stream()
                .map(test -> test.getQuestions())
                .flatMap(list -> list.stream())
                .map(question -> question.toMap())
                .collect(Collectors.toList()));
        }

        return new ResponseEntity(result, HttpStatus.OK);
    }

    @GetMapping("/questions/{id}")
    public ResponseEntity getQuestion(HttpServletRequest request, @PathVariable Long id){
        String token = jwtTokenProvider.resolveToken(request);

        User user = userRepository.findByEmail(jwtTokenProvider.getEmail(token)).get();

        if(!questionRepository.findById(id).isPresent()){
            return new ResponseEntity(
                "Error: Question with id: " + id + " could not be found.", 
                HttpStatus.BAD_REQUEST);
        }

        Question question = questionRepository.findById(id).get();

        if(user.getRoles().get(0).equals("ROLE_ADMIN")){
            return new ResponseEntity(question.toMap(), HttpStatus.OK);
        }
        else{
            boolean permits = user.getTests().stream()
                .map(test -> test.getQuestions())
                .flatMap(list -> list.stream())
                .anyMatch(questionEntity -> {
                    return questionEntity.getId() == id;
                });

            if(permits){
                return new ResponseEntity(question.toMap(), HttpStatus.OK);
            }
            else{
                return new ResponseEntity(HttpStatus.UNAUTHORIZED);
            }
        }
    }
}