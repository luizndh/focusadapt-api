package com.focus.adapt.api.infrastructure.repository;

import com.focus.adapt.api.AbstractIntegrationTest;
import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;
import com.focus.adapt.api.domain.entity.enums.StudyTaskStatus;
import com.focus.adapt.api.domain.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StudyPlanRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyTaskRepository studyTaskRepository;

    @Test
    void shouldSaveAndSoftDeleteStudyPlan() {
        User user = User.builder()
                .name("Plan User")
                .email("plan_user@example.com")
                .password("secured_password")
                .role(UserRole.USER)
                .build();
        user = userRepository.save(user);

        StudyPlan plan = StudyPlan.builder()
                .title("Java Mastery")
                .description("Complete Java course")
                .category("Programming")
                .status(StudyPlanStatus.ACTIVE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .user(user)
                .build();
        StudyPlan savedPlan = studyPlanRepository.save(plan);
        assertNotNull(savedPlan.getId());

        StudyTask task = StudyTask.builder()
                .title("Streams API")
                .description("Learn Java streams")
                .status(StudyTaskStatus.PENDING)
                .sortOrder(1)
                .estimatedDurationMinutes(60)
                .studyPlan(savedPlan)
                .build();
        StudyTask savedTask = studyTaskRepository.save(task);
        savedPlan.getStudyTasks().add(savedTask);

        // Execute soft delete
        studyPlanRepository.delete(savedPlan);
        studyPlanRepository.flush();

        // Due to @SQLRestriction("deleted = false"), findById should return empty
        Optional<StudyPlan> deletedPlan = studyPlanRepository.findById(savedPlan.getId());
        assertFalse(deletedPlan.isPresent());
    }
}
