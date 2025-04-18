package com.appxemphim.firebaseBackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
     
    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .securityMatcher("/**") // Áp dụng cho tất cả các request
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/").permitAll()
            .requestMatchers("/auth/**").permitAll() // Cho phép login/signup
            .requestMatchers("/email/**").permitAll() // Cho phép check email
            .anyRequest().permitAll() // Các request khác cần xác thực (JWT sau)
        )
        .csrf(csrf -> csrf.disable()) // Tắt CSRF cho API REST
        .addFilterBefore(new JwtRequestFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class); // Thêm filter để xác thực JWT

    return http.build();
    }
}
