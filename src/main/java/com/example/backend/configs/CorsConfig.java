package com.example.backend.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    private static final String ALLOWED_ORIGIN = "http://localhost:3000";

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of(ALLOWED_ORIGIN)); // Разрешённые источники
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Разрешённые методы
        corsConfiguration.setAllowedHeaders(List.of("*")); // Разрешённые заголовки
        corsConfiguration.setAllowCredentials(true); // Разрешить учетные данные

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration); // Применить настройки ко всем маршрутам

        return new CorsWebFilter(source);
    }
}
