package com.focus.adapt.api.infrastructure.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.badge-icons.path:/app/badge-icons/}")
    private String badgeIconsPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = badgeIconsPath;
        if (!location.startsWith("file:") && !location.startsWith("classpath:")) {
            Path path = Paths.get(location).toAbsolutePath().normalize();
            String uriString = path.toUri().toString();
            location = uriString.endsWith("/") ? uriString : uriString + "/";
        } else if (!location.endsWith("/")) {
            location += "/";
        }

        log.info("Registering static resource handler: /badge-icons/** -> {}", location);

        registry.addResourceHandler("/badge-icons/**")
                .addResourceLocations(location)
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic());
    }
}
