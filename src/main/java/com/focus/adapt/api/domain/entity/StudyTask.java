package com.focus.adapt.api.domain.entity;

import com.focus.adapt.api.domain.entity.enums.StudyTaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "study_tasks")
public class StudyTask extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order")
    private int sortOrder;

    @Column(name = "estimated_duration_minutes")
    private int estimatedDurationMinutes;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StudyTaskStatus status = StudyTaskStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_plan_id", nullable = false)
    private StudyPlan studyPlan;

    @ManyToMany(mappedBy = "studyTasks")
    @Builder.Default
    private List<StudySession> studySessions = new ArrayList<>();
}
