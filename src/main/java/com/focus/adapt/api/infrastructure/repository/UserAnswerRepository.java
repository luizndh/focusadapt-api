package com.focus.adapt.api.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserAnswer;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, UUID> {
    List<UserAnswer> findByUser(User user);
}
