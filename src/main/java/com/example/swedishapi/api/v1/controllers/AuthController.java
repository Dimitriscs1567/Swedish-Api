package com.example.swedishapi.api.v1.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("api/v1/auth")
public class AuthController{

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WhiteTokenRepository whiteTokenRepository;

    @Autowired 
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SendGrid sendGrid;

    @PostMapping("/signin")
    public ResponseEntity signin(@RequestBody Map<String, String> data) {
        Map<Object, Object> response = new HashMap<>();

        try {
            String email = data.get("email");
            String password = data.get("password");
            if(email == null || password == null || password.equals("")){
                response.put("message", "Request must contain an email and a password");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            
            jwtTokenProvider.deleteOtherTokens(email);
            String[] tokens = jwtTokenProvider.createToken(email, this.userRepository.findByEmail(email).get().getRoles(), false);
            
            whiteTokenRepository.save(new WhiteToken(tokens[0], tokens[1]));

            response.put("token", tokens[0]);
            response.put("refresh", tokens[1]);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AuthenticationException e) {
            response.put("message", "Invalid email/password supplied");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/signout")
    public ResponseEntity logout(HttpServletRequest request){
        String token = jwtTokenProvider.resolveToken(request);
        Map<Object, Object> model = new HashMap<>();

        if(token != null && whiteTokenRepository.findById(token).isPresent()){
            WhiteToken whiteToken = whiteTokenRepository.findById(token).get();
            whiteTokenRepository.delete(whiteToken);
            model.put("message", "Signout completed.");
        }
        else{
            model.put("Error", "Invalid token.");
        }

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @PostMapping("/change_password")
    public ResponseEntity changePassword(@RequestBody Map<String, String> data){

        String email = data.get("email");
        if(email == null){
            Map<String, String> response = new HashMap<>();
            response.put("message", "No email was given");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(email.equals("mobile-app") || !userRepository.findByEmail(email).isPresent()){
            Map<String, String> response = new HashMap<>();
            response.put("message", "No user found with email: " + email);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String[] token = jwtTokenProvider.createToken(email, this.userRepository.findByEmail(email).get().getRoles(), true);
        whiteTokenRepository.save(new WhiteToken(token[0], null));

        Email from = new Email("no-reply@swedishtests.se");
        String subject = "Swedish Tests Change Password";
        Email to = new Email(email);
        Content content = new Content("text/html", "To reset your password please go to the link bellow. This link expires after 30 minutes. "  +
                "Link: https://family-scouting-admin-frontend.herokuapp.com/newpassword?token=" + token[0]);
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

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/new_password")
    public ResponseEntity newPassword(@RequestBody Map<String, String> data){
        Map<String, String> response = new HashMap<>();

        String token = data.get("token");
        String password = data.get("password");

        if(token == null || password == null){
            response.put("message", "Request must contain a token and a password");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(!jwtTokenProvider.validateToken(token)){
            response.put("message", "Given token is invalid.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String email = jwtTokenProvider.getEmail(token);
        User user = userRepository.findByEmail(email).get();
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);

        whiteTokenRepository.deleteById(token);

        response.put("message", "Password changed.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/validate_token")
    public ResponseEntity validateToken(@RequestBody Map<String, String> data){
        Map<String, String> response = new HashMap<>();

        if(!jwtTokenProvider.validateToken(data.get("token"))){
            response.put("message", "Invalid token.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/new_token")
    public ResponseEntity newToken(@RequestBody Map<String, String> data){
        Map<String, String> response = new HashMap<>();

        String refresh = data.get("refresh");
        if(refresh == null || !jwtTokenProvider.validateRefreshToken(refresh)){
            response.put("message", "Invalid refresh token.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        WhiteToken whiteToken = whiteTokenRepository.findByRefresh(refresh).get();
        if(jwtTokenProvider.validateToken(whiteToken.getToken())){
            response.put("message", "You still have a valid token.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(jwtTokenProvider.getEmail(refresh)).get();
        whiteTokenRepository.delete(whiteToken);
        String[] tokens = jwtTokenProvider.createToken(user.getEmail(), user.getRoles(), false);

        whiteTokenRepository.save(new WhiteToken(tokens[0], tokens[1]));

        response.put("token", tokens[0]);
        response.put("refresh", tokens[1]);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}