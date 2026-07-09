package com.focus.adapt.api.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
}
