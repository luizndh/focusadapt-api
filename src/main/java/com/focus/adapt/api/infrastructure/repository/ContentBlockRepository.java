package com.focus.adapt.api.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.focus.adapt.api.domain.entity.ContentBlock;
import com.focus.adapt.api.domain.entity.StudyTask;

@Repository
public interface ContentBlockRepository extends JpaRepository<ContentBlock, UUID> {
    List<ContentBlock> findByStudyTaskOrderBySortOrderAsc(StudyTask studyTask);
}
