package com.focus.adapt.api.infrastructure.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.focus.adapt.api.application.dto.UserRequest;
import com.focus.adapt.api.application.dto.UserResponse;
import com.focus.adapt.api.application.service.TokenService;
import com.focus.adapt.api.application.service.UserService;
import com.focus.adapt.api.infrastructure.repository.UserRepository;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldCreateUserSuccessfully() throws Exception {
        UserRequest request = UserRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password123")
                .build();

        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .email("john.doe@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.createUser(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.name").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldReturnValidationErrorWhenFieldsAreInvalid() throws Exception {
        UserRequest invalidRequest = UserRequest.builder()
                .name("") // Invalid
                .email("invalid-email") // Invalid
                .password("123") // Too short
                .build();

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void shouldGetUserByIdSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = UserResponse.builder()
                .id(userId)
                .name("John Doe")
                .email("john.doe@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.getUserById(userId)).thenReturn(response);

        mockMvc.perform(get("/v1/users/" + userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.name").value("John Doe"));
    }

    @Test
    void shouldRejectQuestionnaireWithDuplicateKeysInJsonPayload() throws Exception {
        String duplicateKeyJson = """
                {
                  "answers": {
                    "P1": 3, "P1": 2,
                    "P2": 3, "P3": 3, "P4": 3, "P5": 3, "P6": 3, "P7": 3, "P8": 3, "P9": 3, "P10": 3,
                    "B1": 1, "B2": 1, "B3": 1, "B4": 1, "B5": 1, "B6": 1, "B7": 1, "B8": 1, "B9": 1, "B10": 1
                  }
                }
                """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/v1/users/me/questionnaire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateKeyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldPersistQuestionnaireForAuthenticatedUserOnlyAndIgnoreClientSuppliedProfile() {
        UserRepository mockRepo = org.mockito.Mockito.mock(UserRepository.class);
        com.focus.adapt.api.application.mapper.UserMapper mockMapper =
                org.mockito.Mockito.mock(com.focus.adapt.api.application.mapper.UserMapper.class);
        org.springframework.security.crypto.password.PasswordEncoder mockEncoder =
                org.mockito.Mockito.mock(org.springframework.security.crypto.password.PasswordEncoder.class);
        com.focus.adapt.api.application.service.ProcrastinationProfileCalculator calculator =
                new com.focus.adapt.api.application.service.ProcrastinationProfileCalculator();

        UserService realUserService = new UserService(mockRepo, mockMapper, mockEncoder, calculator);

        UUID authenticatedUserId = UUID.randomUUID();
        com.focus.adapt.api.domain.entity.User authenticatedUser = com.focus.adapt.api.domain.entity.User.builder()
                .email("student@focusadapt.com")
                .name("Student")
                .password("encoded")
                .onboardingCompleted(true)
                .questionnaireCompleted(false)
                .build();
        authenticatedUser.setId(authenticatedUserId);

        when(mockRepo.findById(authenticatedUserId)).thenReturn(java.util.Optional.of(authenticatedUser));
        when(mockRepo.save(authenticatedUser)).thenReturn(authenticatedUser);
        when(mockMapper.toResponse(authenticatedUser)).thenAnswer(inv -> {
            com.focus.adapt.api.domain.entity.User u = inv.getArgument(0);
            return UserResponse.builder()
                    .id(u.getId())
                    .name(u.getName())
                    .email(u.getEmail())
                    .onboardingCompleted(u.isOnboardingCompleted())
                    .questionnaireCompleted(u.isQuestionnaireCompleted())
                    .procrastinationProfile(u.getProcrastinationProfile() != null ? u.getProcrastinationProfile().name() : null)
                    .build();
        });

        java.util.Map<String, Integer> answers = new java.util.LinkedHashMap<>();
        for (int i = 1; i <= 10; i++) {
            answers.put("P" + i, 1);
            answers.put("B" + i, 3);
        }

        // Cliente envia perfil contraditório PERFECTIONIST, mas as respostas indicam STIMULUS_SEEKER
        com.focus.adapt.api.application.dto.QuestionnaireSubmitRequest submitRequest =
                com.focus.adapt.api.application.dto.QuestionnaireSubmitRequest.builder()
                        .answers(answers)
                        .profile(com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile.PERFECTIONIST)
                        .completed(true)
                        .build();

        UserResponse updated = realUserService.submitQuestionnaire(authenticatedUser, submitRequest);

        org.junit.jupiter.api.Assertions.assertEquals(authenticatedUserId, updated.getId());
        org.junit.jupiter.api.Assertions.assertTrue(updated.isQuestionnaireCompleted());
        org.junit.jupiter.api.Assertions.assertEquals("STIMULUS_SEEKER", updated.getProcrastinationProfile());

        // Simula novo login / carregamento de perfil (GET /v1/users/me)
        UserResponse afterRelogin = realUserService.getCurrentUser(authenticatedUserId);
        org.junit.jupiter.api.Assertions.assertTrue(afterRelogin.isQuestionnaireCompleted());
        org.junit.jupiter.api.Assertions.assertEquals("STIMULUS_SEEKER", afterRelogin.getProcrastinationProfile());
    }
}

