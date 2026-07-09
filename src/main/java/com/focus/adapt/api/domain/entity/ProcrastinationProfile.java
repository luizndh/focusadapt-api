package com.focus.adapt.api.domain.entity;

import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfileType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "procrastination_profiles")
public class ProcrastinationProfile extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", nullable = false, unique = true)
    private ProcrastinationProfileType profileType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String characteristics;

    @Column(columnDefinition = "TEXT")
    private String tips;
}
