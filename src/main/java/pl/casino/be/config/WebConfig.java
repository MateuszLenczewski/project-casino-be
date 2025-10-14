package pl.casino.be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Apply this configuration to all API endpoints
                registry.addMapping("/api/**")
                        // Allow requests specifically from your frontend's origin
                        .allowedOrigins("http://localhost:3000")
                        // Allow standard HTTP methods
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // Allow all headers (like Authorization, Content-Type)
                        .allowedHeaders("*")
                        // Allow credentials (needed for sessions/cookies, good practice)
                        .allowCredentials(true);
            }
        };
    }
}