package eda.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${cors-hostname}")
    String hostName;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("CORS hostname: " + hostName);
        registry.addMapping("/**")
                .allowedOrigins(hostName);
    }
}
