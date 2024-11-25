package com.example.backend.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig{

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Более краткий способ отключения CSRF
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // Отключить HTTP Basic
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/", "/login", "favicon.ico", "/Films").permitAll() // Разрешить доступ без аутентификации
                        .anyExchange().authenticated()
                )
                .formLogin(formLoginSpec -> formLoginSpec
                                .loginPage("/login")
                        // .authenticationFailureUrl("/login?error") // Укажите URL для отображения ошибки
                )
                .build();
    }
}
