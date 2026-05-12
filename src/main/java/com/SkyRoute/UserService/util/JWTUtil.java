package com.SkyRoute.UserService.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import com.SkyRoute.UserService.Entity.User;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

@Component
public class JWTUtil {
	
    //sign the jwt when generating it and verify the jwt when validating 
    private final String SECRET = "mysecretkeymysecretkeymysecretkey123"; // ✅ at least 32 chars
   
    //This converts the string secret -> cryptographic key object
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
  //JWT with 3 componenets Header,payload,Signature
    // Generate token with username as subject
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail()) // still OK
                .claim("userId", user.getUserId())  // <-- ADD THIS
                .claim("role", user.getRole().name())
                .claim("roles", List.of(user.getRole().name()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 3))
                .signWith(key, SignatureAlgorithm.HS256) //Hash-based message authentication code secure hash algorithm with 256 bit hash
                .compact();
    }
    
//    HMACSHA256(
//    		  base64UrlEncode(header) + "." + base64UrlEncode(payload),
//    		  SECRET_KEY
//    		)
    
    // ✅ Extract username from token
    public String extractEmail(String token) {
    	
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validate token
    public boolean validateToken(String token, String expectedSubject) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String actualSubject = claims.getSubject();
            return expectedSubject != null && expectedSubject.equals(actualSubject);
        } catch (ExpiredJwtException e) {
            // token expired
            return false;
        } catch (JwtException e) {
            // invalid signature / malformed / unsupported, etc.
            return false;
        }
    }
    
}