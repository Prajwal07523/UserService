package com.SkyRoute.UserService.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.SkyRoute.UserService.Entity.User;
import com.SkyRoute.UserService.Repository.UserRepository;
import com.SkyRoute.UserService.util.JWTUtil;

import io.jsonwebtoken.ExpiredJwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;
    

@Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

return path.startsWith("/auth/login") ||
           path.startsWith("/auth/register") ||
           path.startsWith("/swagger-ui") ||
           path.startsWith("/v3/api-docs");

    }

    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
       
        String path = request.getRequestURI();
        if (path.startsWith("/auth")) {
               filterChain.doFilter(request, response);
               return;
           }

        
        // Expected format: "Bearer <JWT>"
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();  //removes Bearer from token

            try {
                // Make email effectively final by declaring it here and not reassigning
                final String email = jwtUtil.extractEmail(token);
                
                //securityContextHolder stores security info of the current request
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtUtil.validateToken(token, email)) { //checks signature,expiration,correct user

                        User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
                        //Represents the permission or role granted to user
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                         
                        //This object represents the loggedin user
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(authority));
                        //This tells user is autheticated and now controllers can access
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (ExpiredJwtException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has expired. Please log in again.");
                return;
            } catch (Exception ex) { // Handle other exceptions
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized access.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}