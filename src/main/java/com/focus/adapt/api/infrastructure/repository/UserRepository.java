package com.focus.adapt.api.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.procrastinationProfile = :profile AND u.deleted = false ORDER BY u.points DESC, u.createdAt ASC")
    List<User> findTopByProcrastinationProfileOrderByPointsDesc(
        @Param("profile") ProcrastinationProfile profile,
        Pageable pageable
    );
}
