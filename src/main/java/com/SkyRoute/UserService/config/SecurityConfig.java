package com.SkyRoute.UserService.config;

import com.SkyRoute.UserService.filter.JWTFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JWTFilter jwtFilter;

    public SecurityConfig(JWTFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    // Provide your own PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // strength 10
    }
    
    
    @Bean  //This method configures all security rules
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Stateless JWT
            .csrf(csrf -> csrf.disable()) //csrf is needed for session bsed we are using jwt stateless
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //do not create server sessions

            // Authorization rules
            .authorizeHttpRequests(registry -> registry
                .requestMatchers("/auth/**", "/actuator/health").permitAll()

                .requestMatchers(
                               "/swagger-ui.html",
                               "/swagger-ui/**",
                               "/v3/api-docs/**"
                           ).permitAll()

                .anyRequest().authenticated()
            )

            // Disable form login & http basic (we use JWT)
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}