package com.focus.adapt.api.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.AnswerOption;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, UUID> {
}
