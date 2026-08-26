package com.focus.adapt.api.domain.entity;

import com.focus.adapt.api.domain.entity.enums.ContentBlockType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
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
@Table(name = "content_blocks")
public class ContentBlock extends BaseEntity {

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", nullable = false)
    private ContentBlockType blockType;

    @Column(nullable = false)
    private String title;

    @Column(name = "estimated_minutes", nullable = false)
    @Builder.Default
    private int estimatedMinutes = 5;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_task_id", nullable = false)
    private StudyTask studyTask;
}
