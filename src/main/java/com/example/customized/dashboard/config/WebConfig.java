package com.example.customized.dashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Marks this class as a configuration class for Spring
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures Cross-Origin Resource Sharing (CORS) for the application.
     * This allows the backend to handle requests from a different origin, typically the frontend.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Allow CORS for all URL paths
                .allowedOrigins("http://localhost:3000") // Specify the allowed origin (frontend URL)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Specify allowed HTTP methods
                .allowedHeaders("*") // Allow all headers in the request
                .allowCredentials(true); // Enable credentials (e.g., cookies, authorization headers)
    }
}

