package com.focus.adapt.api.domain.entity;

import com.focus.adapt.api.domain.entity.enums.StudyMode;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "study_sessions")
@SQLDelete(sql = "UPDATE study_sessions SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class StudySession extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "study_mode", nullable = false)
    @Builder.Default
    private StudyMode studyMode = StudyMode.FREE;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private int durationMinutes = 0;

    @Column(name = "effective_study_minutes", nullable = false)
    @Builder.Default
    private int effectiveStudyMinutes = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudySessionStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Pomodoro-specific fields (relevant when studyMode = POMODORO)
    @Column(name = "work_duration_minutes", nullable = false)
    @Builder.Default
    private int workDurationMinutes = 0;

    @Column(name = "break_duration_minutes", nullable = false)
    @Builder.Default
    private int breakDurationMinutes = 0;

    @Column(name = "completed_cycles", nullable = false)
    @Builder.Default
    private int completedCycles = 0;

    @Column(name = "total_pauses_count", nullable = false)
    @Builder.Default
    private int totalPausesCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_plan_id")
    private StudyPlan studyPlan;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "study_session_tasks",
        joinColumns = @JoinColumn(name = "study_session_id"),
        inverseJoinColumns = @JoinColumn(name = "study_task_id")
    )
    @Builder.Default
    private List<StudyTask> studyTasks = new ArrayList<>();
}
