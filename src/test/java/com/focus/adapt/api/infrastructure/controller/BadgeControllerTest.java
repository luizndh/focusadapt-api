package com.focus.adapt.api.infrastructure.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.focus.adapt.api.application.dto.BadgeResponse;
import com.focus.adapt.api.application.dto.UserAchievementResponse;
import com.focus.adapt.api.application.service.BadgeService;
import com.focus.adapt.api.application.service.TokenService;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.BadgeCategory;
import com.focus.adapt.api.domain.entity.enums.UserRole;
import com.focus.adapt.api.infrastructure.exception.ResourceNotFoundException;
import com.focus.adapt.api.infrastructure.repository.UserRepository;
import com.focus.adapt.api.infrastructure.security.JwtAuthenticationFilter;
import com.focus.adapt.api.infrastructure.security.SecurityConfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@WebMvcTest(BadgeController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class BadgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private BadgeService badgeService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    private User testUser;
    private BadgeResponse sampleBadge;

    @BeforeEach
    void setUp() throws Exception {
        testUser = User.builder()
                .email("user@example.com")
                .name("Test User")
                .role(UserRole.USER)
                .build();
        testUser.setId(UUID.randomUUID());

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        Mockito.doAnswer(invocation -> {
            ServletRequest req = invocation.getArgument(0);
            ServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        sampleBadge = BadgeResponse.builder()
                .id(UUID.randomUUID())
                .code("STUDY_PLAN_1")
                .name("Primeiro Plano")
                .description("Conclua seu primeiro plano de estudos.")
                .category(BadgeCategory.STUDY_PLAN)
                .threshold(1)
                .xpReward(20)
                .iconUrl("/badge-icons/study-plan-1.png")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldDenyAccessToBadgesWhenAnonymous() throws Exception {
        mockMvc.perform(get("/v1/badges"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(
            value = "user@example.com",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void shouldGetPaginatedBadges() throws Exception {
        Page<BadgeResponse> page = new PageImpl<>(List.of(sampleBadge));
        when(badgeService.getBadges(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/v1/badges")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].code").value("STUDY_PLAN_1"))
                .andExpect(jsonPath("$.data.content[0].name").value("Primeiro Plano"))
                .andExpect(jsonPath("$.data.content[0].iconUrl").value("/badge-icons/study-plan-1.png"));
    }

    @Test
    @WithUserDetails(
            value = "user@example.com",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void shouldGetBadgeById() throws Exception {
        UUID badgeId = sampleBadge.getId();
        when(badgeService.getBadgeById(badgeId)).thenReturn(sampleBadge);

        mockMvc.perform(get("/v1/badges/" + badgeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(badgeId.toString()))
                .andExpect(jsonPath("$.data.code").value("STUDY_PLAN_1"))
                .andExpect(jsonPath("$.data.xpReward").value(20));
    }

    @Test
    @WithUserDetails(
            value = "user@example.com",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void shouldReturn404WhenBadgeNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        when(badgeService.getBadgeById(randomId)).thenThrow(new ResourceNotFoundException("Badge not found"));

        mockMvc.perform(get("/v1/badges/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithUserDetails(
            value = "user@example.com",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void shouldGetMyAchievements() throws Exception {
        UserAchievementResponse achievement = UserAchievementResponse.builder()
                .id(UUID.randomUUID())
                .badgeId(sampleBadge.getId())
                .badgeCode("STUDY_PLAN_1")
                .badgeName("Primeiro Plano")
                .badgeDescription("Conclua seu primeiro plano de estudos.")
                .badgeCategory(BadgeCategory.STUDY_PLAN)
                .badgeThreshold(1)
                .badgeXpReward(20)
                .badgeIconUrl("/badge-icons/study-plan-1.png")
                .unlockedAt(LocalDateTime.now())
                .build();

        Page<UserAchievementResponse> page = new PageImpl<>(List.of(achievement));
        when(badgeService.getMyAchievements(any(User.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/v1/badges/my-achievements")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].badgeCode").value("STUDY_PLAN_1"))
                .andExpect(jsonPath("$.data.content[0].badgeIconUrl").value("/badge-icons/study-plan-1.png"));
    }
}
