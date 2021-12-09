package eda.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${cors-hostname}")
    String[] hostName;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("CORS hostname: " + Arrays.toString(hostName));
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "PUT", "OPTIONS", "HEAD")
                .allowedOrigins(hostName);
    }
}
