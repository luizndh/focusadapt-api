package com.focus.adapt.api.application.service;

import java.time.LocalDate;
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
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.focus.adapt.api.application.dto.DashboardResponse;
import com.focus.adapt.api.application.mapper.BadgeMapper;
import com.focus.adapt.api.application.mapper.MissionMapper;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
import com.focus.adapt.api.domain.entity.enums.UserRole;
import com.focus.adapt.api.infrastructure.repository.StudySessionRepository;
import com.focus.adapt.api.infrastructure.repository.UserAchievementRepository;
import com.focus.adapt.api.infrastructure.repository.UserMissionRepository;
import com.focus.adapt.api.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private UserMissionRepository userMissionRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private MissionMapper missionMapper;

    @Mock
    private BadgeMapper badgeMapper;

    @InjectMocks
    private DashboardService dashboardService;

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
        when(userMissionRepository.findByUserAndStatus(any(User.class), eq(UserMissionStatus.IN_PROGRESS)))
                .thenReturn(Collections.emptyList());
        when(userAchievementRepository.findByUser(any(User.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(studySessionRepository.findByUserAndStatus(any(User.class), eq(StudySessionStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(Page.empty());

        DashboardResponse response = dashboardService.getDashboard(testUser);

        assertNotNull(response);
        assertEquals("Test User", response.getUserName());
        assertEquals(100, response.getXpPoints());
        assertEquals(5, response.getStreak());
    }

    @Test
    void shouldReturnZeroStreakWhenLastStudyDateIsMoreThanOneDayAgo() {
        testUser.setLastStudyDate(LocalDate.now().minusDays(2));

        when(userMissionRepository.findByUserAndStatus(any(User.class), eq(UserMissionStatus.IN_PROGRESS)))
                .thenReturn(Collections.emptyList());
        when(userAchievementRepository.findByUser(any(User.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(studySessionRepository.findByUserAndStatus(any(User.class), eq(StudySessionStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(Page.empty());

        DashboardResponse response = dashboardService.getDashboard(testUser);

        assertNotNull(response);
        assertEquals(0, response.getStreak());
    }

    @Test
    void shouldReturnLeaderboardForStimulusSeeker() {
        testUser.setProcrastinationProfile(com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile.STIMULUS_SEEKER);
        testUser.setId(java.util.UUID.randomUUID());

        User otherUser = User.builder()
                .name("Top Player")
                .points(800)
                .procrastinationProfile(com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile.STIMULUS_SEEKER)
                .build();
        otherUser.setId(java.util.UUID.randomUUID());

        when(userRepository.findTopByProcrastinationProfileOrderByPointsDesc(
                eq(com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile.STIMULUS_SEEKER),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(java.util.List.of(otherUser, testUser));

        when(userMissionRepository.findByUserAndStatus(any(User.class), eq(UserMissionStatus.IN_PROGRESS)))
                .thenReturn(Collections.emptyList());
        when(userAchievementRepository.findByUser(any(User.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(studySessionRepository.findByUserAndStatus(any(User.class), eq(StudySessionStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(Page.empty());

        DashboardResponse response = dashboardService.getDashboard(testUser);

        assertNotNull(response);
        assertNotNull(response.getLeaderboard());
        assertEquals(2, response.getLeaderboard().size());

        var first = response.getLeaderboard().get(0);
        assertEquals("Top Player", first.getUserName());
        assertEquals(1, first.getRank());
        assertEquals(800, first.getXpPoints());
        assertEquals(5, first.getLevel()); // 4 + (800-500)/300 = 5
        org.junit.jupiter.api.Assertions.assertFalse(first.isCurrentUser());

        var second = response.getLeaderboard().get(1);
        assertEquals("Test User", second.getUserName());
        assertEquals(2, second.getRank());
        assertEquals(100, second.getXpPoints());
        assertEquals(2, second.getLevel()); // points < 250 -> 2
        org.junit.jupiter.api.Assertions.assertTrue(second.isCurrentUser());
    }

    @Test
    void shouldReturnEmptyLeaderboardForPerfectionist() {
        testUser.setProcrastinationProfile(com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile.PERFECTIONIST);

        when(userMissionRepository.findByUserAndStatus(any(User.class), eq(UserMissionStatus.IN_PROGRESS)))
                .thenReturn(Collections.emptyList());
        when(userAchievementRepository.findByUser(any(User.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(studySessionRepository.findByUserAndStatus(any(User.class), eq(StudySessionStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(Page.empty());

        DashboardResponse response = dashboardService.getDashboard(testUser);

        assertNotNull(response);
        assertNotNull(response.getLeaderboard());
        org.junit.jupiter.api.Assertions.assertTrue(response.getLeaderboard().isEmpty());
    }
}
