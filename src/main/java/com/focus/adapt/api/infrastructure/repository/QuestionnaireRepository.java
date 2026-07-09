package com.focus.adapt.api.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.Questionnaire;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, UUID> {
    Page<Questionnaire> findByActive(boolean active, Pageable pageable);
}
