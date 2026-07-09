package com.focus.adapt.api.domain.entity;

import com.focus.adapt.api.domain.entity.enums.PomodoroStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pomodoro_sessions")
public class PomodoroSession extends BaseEntity {

    @Column(name = "work_duration_minutes", nullable = false)
    private int workDurationMinutes;

    @Column(name = "break_duration_minutes", nullable = false)
    private int breakDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PomodoroStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "completed_cycles", nullable = false)
    @Builder.Default
    private int completedCycles = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(mappedBy = "pomodoroSession", fetch = FetchType.LAZY)
    private StudySession studySession;
}
