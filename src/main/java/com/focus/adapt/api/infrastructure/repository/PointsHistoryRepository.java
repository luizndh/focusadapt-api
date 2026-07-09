package com.focus.adapt.api.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.PointsHistory;
import com.focus.adapt.api.domain.entity.User;

public interface PointsHistoryRepository extends JpaRepository<PointsHistory, UUID> {
    Page<PointsHistory> findByUser(User user, Pageable pageable);
}
