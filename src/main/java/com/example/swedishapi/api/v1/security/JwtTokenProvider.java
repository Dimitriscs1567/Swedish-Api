package com.example.swedishapi.api.v1.security;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.example.swedishapi.api.v1.repositories.WhiteTokenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private long validityInMilliseconds = 7200000; // 2h

    private long shortValidityInMilliseconds = 1800000; // 30m

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    WhiteTokenRepository whiteTokenRepository;

    @Value("${jwt.secret}")
    private String signKey;

    public String[] createToken(String username, List<String> roles, boolean shortValidity){
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);
        Date now = new Date();
        Date validity = shortValidity 
                            ? new Date(now.getTime() + shortValidityInMilliseconds) 
                            : new Date(now.getTime() + validityInMilliseconds);

        if(shortValidity){
            String[] result = {
                Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(validity)
                    .signWith(getSignKey(), SignatureAlgorithm.HS256)
                    .compact()
            };

            return result;
        }
        else{
            String[] result = {
                Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(validity)
                    .signWith(getSignKey(), SignatureAlgorithm.HS256)
                    .compact(),

                Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .signWith(getSignKey(), SignatureAlgorithm.HS256)
                    .compact()
            };

            return result;
        }
    }

	public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }

        return null;
	}

	public boolean validateToken(String token) {
        if(whiteTokenRepository.findById(token).isPresent()){
            try{
                Jws<Claims> claims = Jwts.parser().setSigningKey(getSignKey()).parseClaimsJws(token);

                if(claims.getBody().getExpiration() != null){
                    if(claims.getBody().getExpiration().before(new Date())){
                        return false;
                    }
                }

                return true;
            } catch(JwtException | IllegalArgumentException e) {
                return false;
            }
        }

        return false;
	}

	public Authentication getAuthentication(String token) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getEmail(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    //Throws exception so it MUST be used only after validate token.
    public String getEmail(String token) {
        return Jwts.parser().setSigningKey(getSignKey()).parseClaimsJws(token).getBody().getSubject();
    }

    public Key getSignKey(){
        byte[] keyBytes = Base64.getDecoder().decode(signKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return key;
    }

    public void deleteOtherTokens(String email){
        for(var  token : whiteTokenRepository.findAll()){
            if(!validateToken(token.getToken()) || getEmail(token.getToken()).equals(email)){
                whiteTokenRepository.delete(token);
            }
        }
    }
}
