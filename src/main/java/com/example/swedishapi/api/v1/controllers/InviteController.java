package com.example.swedishapi.api.v1.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.swedishapi.api.v1.entities.User;
import com.example.swedishapi.api.v1.entities.WhiteToken;
import com.example.swedishapi.api.v1.repositories.UserRepository;
import com.example.swedishapi.api.v1.repositories.WhiteTokenRepository;
import com.example.swedishapi.api.v1.security.JwtTokenProvider;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("api/v1")
public class InviteController{

    @Autowired 
    private UserRepository userRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    WhiteTokenRepository whiteTokenRepository;

    @Autowired
    private SendGrid sendGrid;

    @PostMapping("/invite_new_admin")
    public ResponseEntity inviteNewAdmin(@RequestBody Map<String, String> data){

        String email = data.get("email");
        if(email == null){
            Map<String, String> response = new HashMap<>();
            response.put("message", "No email was given");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        boolean registeredEmail = userRepository.findByEmail(email).isPresent();
        if(registeredEmail){
            if(userRepository.findByEmail(email).get().getPassword().isEmpty()){
                userRepository.delete(userRepository.findByEmail(email).get());
            }
            else{
                Map<String, String> response = new HashMap<>();
                response.put("message", "Email already exists");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }

        User user = new User();
        user.setEmail(email);
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_ADMIN");
        user.setRoles(roles);
        user.setPassword("");

        String[] token = jwtTokenProvider.createToken(email, roles, true);

        Email from = new Email("no-reply@familyscouting.se");
        String subject = "Family-Scouting Admin Invite";
        Email to = new Email(email);
        Content content = new Content("text/html", "You have been invited to join family-scouting application as an admin. " 
                + "Please press the link bellow to create your own password and complete registration.\n"  
                + "https://family-scouting-admin-frontend.herokuapp.com/newpassword?token=" + token[0]);
        Mail mail = new Mail(from, subject, to, content);

        try{
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        }catch(IOException e){
            Map<String, String> response = new HashMap<>();
            response.put("message", "The email could not be sent. " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
        }

        userRepository.save(user);
        whiteTokenRepository.save(new WhiteToken(token[0], null));

        return new ResponseEntity<>("Email sent to: " + email, HttpStatus.OK);
    }

    @PostMapping("/invite_new_user")
    public ResponseEntity inviteNewUser(@RequestBody Map<String, String> data){

        String email = data.get("email");
        if(email == null){
            Map<String, String> response = new HashMap<>();
            response.put("message", "No email was given");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        boolean registeredEmail = userRepository.findByEmail(email).isPresent();
        if(registeredEmail){
            if(userRepository.findByEmail(email).get().getPassword().isEmpty()){
                userRepository.delete(userRepository.findByEmail(email).get());
            }
            else{
                Map<String, String> response = new HashMap<>();
                response.put("message", "Email already exists");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }

        User user = new User();
        user.setEmail(email);
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");
        user.setRoles(roles);
        user.setPassword("");

        String[] token = jwtTokenProvider.createToken(email, roles, true);

        Email from = new Email("no-reply@familyscouting.se");
        String subject = "Family-Scouting Admin Invite";
        Email to = new Email(email);
        Content content = new Content("text/html", "You have been invited to join family-scouting application as an admin. " 
                + "Please press the link bellow to create your own password and complete registration.\n"  
                + "https://family-scouting-admin-frontend.herokuapp.com/newpassword?token=" + token[0]);
        Mail mail = new Mail(from, subject, to, content);

        try{
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        }catch(IOException e){
            Map<String, String> response = new HashMap<>();
            response.put("message", "The email could not be sent. " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
        }

        userRepository.save(user);
        whiteTokenRepository.save(new WhiteToken(token[0], null));

        return new ResponseEntity<>("Email sent to: " + email, HttpStatus.OK);
    }
}