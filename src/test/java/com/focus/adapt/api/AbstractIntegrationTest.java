package com.focus.adapt.api;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class AbstractIntegrationTest {

    private static final PostgreSQLContainer<?> postgres;
    private static boolean dockerAvailable = false;

    static {
        PostgreSQLContainer<?> container = null;
        try {
            if (isDockerRunning()) {
                container = new PostgreSQLContainer<>("postgres:15-alpine")
                        .withDatabaseName("focusadapt_test")
                        .withUsername("test")
                        .withPassword("test");
                container.start();
                dockerAvailable = true;
                System.out.println("AbstractIntegrationTest: PostgreSQL Testcontainer started successfully.");
            } else {
                System.out.println("AbstractIntegrationTest: Docker is offline. Falling back to H2 database.");
            }
        } catch (Throwable e) {
            System.err.println("AbstractIntegrationTest: Failed to launch Testcontainers. Falling back to H2. Error: " + e.getMessage());
        }
        postgres = container;
    }

    private static boolean isDockerRunning() {
        try {
            // Check if we can invoke docker command successfully
            Process process = Runtime.getRuntime().exec("docker ps");
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (dockerAvailable && postgres != null) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        } else {
            // Fallback configuration for H2 in PostgreSQL compatibility mode
            registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
            registry.add("spring.datasource.username", () -> "sa");
            registry.add("spring.datasource.password", () -> "");
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        }
    }
}
