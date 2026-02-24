package com.demo.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // Disable CSRF for API endpoints (use tokens in production)
            .authorizeHttpRequests(authz -> authz
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                .antMatchers("/api/**", "/", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(); // Use HTTP Basic Auth for admin endpoints
        
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // In production, use a proper user store (database, LDAP, etc.)
        // and externalize credentials to environment variables or secrets management
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123")) // Change in production!
            .roles("ADMIN")
            .build();
        
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// Made with Bob