package com.example.backend.configs;

import com.example.backend.utilites.JwtAuthenticationManager;
import com.example.backend.utilites.JwtSecurityContextRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig{

    private final JwtAuthenticationManager jwtAuthenticationManager;

    public SecurityConfig(JwtAuthenticationManager jwtAuthenticationManager) {
        this.jwtAuthenticationManager = jwtAuthenticationManager;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Более краткий способ отключения CSRF
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // Отключить HTTP Basic
                .authenticationManager(jwtAuthenticationManager) // Настраиваем менеджер аутентификации
                .securityContextRepository(new JwtSecurityContextRepository(jwtAuthenticationManager)) // Настраиваем репозиторий
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers("/", "/acc/**", "/films/**").permitAll() // Разрешить доступ без аутентификации
                        .anyExchange().authenticated()
                )
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
