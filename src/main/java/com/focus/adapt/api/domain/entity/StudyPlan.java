package com.focus.adapt.api.domain.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "study_plans")
@SQLDelete(sql = "UPDATE study_plans SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class StudyPlan extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_estimated_duration_minutes", nullable = false)
    @Builder.Default
    private int totalEstimatedDurationMinutes = 0;

    @Column(name = "available_hours_per_day", nullable = false)
    @Builder.Default
    private int availableHoursPerDay = 2;

    @Column(name = "available_days")
    @Builder.Default
    private String availableDays = "Mon,Tue,Wed,Thu,Fri,Sat,Sun";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyPlanStatus status;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "studyPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudySession> studySessions = new ArrayList<>();

    @OneToMany(mappedBy = "studyPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudyTask> studyTasks = new ArrayList<>();
}
