package com.focus.adapt.api.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.ProcrastinationProfile;
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfileType;

public interface ProcrastinationProfileRepository extends JpaRepository<ProcrastinationProfile, UUID> {
    Optional<ProcrastinationProfile> findByProfileType(ProcrastinationProfileType profileType);
}
