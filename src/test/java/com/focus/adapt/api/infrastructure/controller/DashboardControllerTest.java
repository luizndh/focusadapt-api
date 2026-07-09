package com.focus.adapt.api.infrastructure.controller;

import com.focus.adapt.api.application.mapper.*;
import com.focus.adapt.api.application.service.TokenService;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
import com.focus.adapt.api.infrastructure.repository.*;
import com.focus.adapt.api.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudyPlanRepository studyPlanRepository;

    @MockitoBean
    private UserMissionRepository userMissionRepository;

    @MockitoBean
    private UserAchievementRepository userAchievementRepository;

    @MockitoBean
    private NotificationRepository notificationRepository;

    @MockitoBean
    private PomodoroSessionRepository pomodoroSessionRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private StudyPlanMapper studyPlanMapper;

    @MockitoBean
    private MissionMapper missionMapper;

    @MockitoBean
    private BadgeMapper badgeMapper;

    @MockitoBean
    private NotificationMapper notificationMapper;

    @MockitoBean
    private QuestionnaireMapper questionnaireMapper;

    @Test
    void shouldDenyAccessToDashboardWhenAnonymous() throws Exception {
        mockMvc.perform(get("/v1/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetDashboardSuccessfully() throws Exception {
        User user = User.builder()
                .name("Bob")
                .points(150)
                .streak(2)
                .build();

        // Stub repository calls
        when(studyPlanRepository.findByUserAndStatus(any(User.class), eq(StudyPlanStatus.ACTIVE), any(PageRequest.class)))
                .thenReturn(Page.empty());
        when(userMissionRepository.findByUserAndStatus(any(User.class), eq(UserMissionStatus.IN_PROGRESS)))
                .thenReturn(Collections.emptyList());
        when(userAchievementRepository.findByUser(any(User.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(notificationRepository.findByUserAndRead(any(User.class), eq(false), any(PageRequest.class)))
                .thenReturn(Page.empty());
        when(pomodoroSessionRepository.findByUser(any(User.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/v1/dashboard")
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userName").value("Bob"))
                .andExpect(jsonPath("$.data.xpPoints").value(150))
                .andExpect(jsonPath("$.data.streak").value(2));
    }
}
