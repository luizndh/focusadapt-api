package com.focus.adapt.api.infrastructure.repository;

import com.focus.adapt.api.AbstractIntegrationTest;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        User user = User.builder()
                .name("Integration User")
                .email("integration@example.com")
                .password("secured_password")
                .role(UserRole.USER)
                .points(100)
                .streak(1)
                .build();

        User saved = userRepository.save(user);
        assertNotNull(saved.getId());

        Optional<User> found = userRepository.findByEmail("integration@example.com");
        assertTrue(found.isPresent());
        assertEquals("Integration User", found.get().getName());
        assertEquals(100, found.get().getPoints());
    }

    @Test
    void shouldReturnEmptyWhenFindingByNonExistingEmail() {
        Optional<User> found = userRepository.findByEmail("ghost@example.com");
        assertFalse(found.isPresent());
    }
}
