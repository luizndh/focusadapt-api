package com.focus.adapt.api.infrastructure.config;

import com.focus.adapt.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import jakarta.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=always"
})
class SchemaValidationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSuccessfullyValidateSchemaAgainstAllEntities() {
        // If Hibernate schema validation failed against schema.sql, the application context would not load.
        assertNotNull(entityManager, "EntityManager must be initialized and valid against schema.sql");
    }
}
