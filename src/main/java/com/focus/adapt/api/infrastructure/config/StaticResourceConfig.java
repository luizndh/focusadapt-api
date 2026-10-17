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
        String location = normalizeLocation(badgeIconsPath);

        log.info("Registering static resource handler: /badge-icons/** -> {}", location);

        String devRelative = normalizeLocation("../badge-icons/");
        String devCurrent = normalizeLocation("badge-icons/");

        registry.addResourceHandler("/badge-icons/**")
                .addResourceLocations(location, devRelative, devCurrent)
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic());
    }

    private String normalizeLocation(String rawLocation) {
        if (rawLocation == null || rawLocation.isBlank()) {
            return "file:badge-icons/";
        }
        if (rawLocation.startsWith("file:") || rawLocation.startsWith("classpath:")) {
            return rawLocation.endsWith("/") ? rawLocation : rawLocation + "/";
        }
        Path path = Paths.get(rawLocation).toAbsolutePath().normalize();
        String uriString = path.toUri().toString();
        return uriString.endsWith("/") ? uriString : uriString + "/";
    }
}
