package com.focus.adapt.api.domain.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.focus.adapt.api.domain.entity.enums.StudyTaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

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

    @jakarta.persistence.OneToMany(mappedBy = "studyTask", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @jakarta.persistence.OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ContentBlock> contentBlocks = new ArrayList<>();

    public void setContentBlocks(List<ContentBlock> blocks) {
        this.contentBlocks.clear();
        if (blocks != null) {
            this.contentBlocks.addAll(blocks);
        }
    }

    public void addContentBlock(ContentBlock block) {
        if (block != null) {
            this.contentBlocks.add(block);
            block.setStudyTask(this);
        }
    }

    public void removeContentBlock(ContentBlock block) {
        if (block != null) {
            this.contentBlocks.remove(block);
            block.setStudyTask(null);
        }
    }
}
