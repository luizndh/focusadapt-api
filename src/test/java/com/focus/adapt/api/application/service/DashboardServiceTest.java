package com.focus.adapt.api.application.service;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.focus.adapt.api.application.dto.DashboardResponse;
import com.focus.adapt.api.application.mapper.BadgeMapper;
import com.focus.adapt.api.application.mapper.MissionMapper;
import com.focus.adapt.api.application.mapper.NotificationMapper;
import com.focus.adapt.api.application.mapper.QuestionnaireMapper;
import com.focus.adapt.api.application.mapper.StudyPlanMapper;
import com.focus.adapt.api.application.service.impl.DashboardServiceImpl;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
import com.focus.adapt.api.domain.entity.enums.UserRole;
import com.focus.adapt.api.infrastructure.repository.NotificationRepository;
import com.focus.adapt.api.infrastructure.repository.PomodoroSessionRepository;
import com.focus.adapt.api.infrastructure.repository.StudyPlanRepository;
import com.focus.adapt.api.infrastructure.repository.UserAchievementRepository;
import com.focus.adapt.api.infrastructure.repository.UserMissionRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private StudyPlanRepository studyPlanRepository;

    @Mock
    private UserMissionRepository userMissionRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PomodoroSessionRepository pomodoroSessionRepository;

    @Mock
    private StudyPlanMapper studyPlanMapper;

    @Mock
    private MissionMapper missionMapper;

    @Mock
    private BadgeMapper badgeMapper;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private QuestionnaireMapper questionnaireMapper;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("user@example.com")
                .name("Test User")
                .points(100)
                .streak(5)
                .role(UserRole.USER)
                .build();
    }

    @Test
    void shouldGetDashboardSuccessfully() {
        when(studyPlanRepository.findByUserAndStatus(any(User.class), eq(StudyPlanStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(userMissionRepository.findByUserAndStatus(any(User.class), eq(UserMissionStatus.IN_PROGRESS)))
                .thenReturn(Collections.emptyList());
        when(userAchievementRepository.findByUser(any(User.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(notificationRepository.findByUserAndIsRead(any(User.class), eq(false), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(pomodoroSessionRepository.findByUser(any(User.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        DashboardResponse response = dashboardService.getDashboard(testUser);

        assertNotNull(response);
        assertEquals("Test User", response.getUserName());
        assertEquals(100, response.getXpPoints());
        assertEquals(5, response.getStreak());

        verify(studyPlanRepository).findByUserAndStatus(any(User.class), eq(StudyPlanStatus.ACTIVE), any(Pageable.class));
    }
}
