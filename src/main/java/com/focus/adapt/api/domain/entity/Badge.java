package com.focus.adapt.api.domain.entity;

import com.focus.adapt.api.domain.entity.enums.BadgeCategory;

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
@Table(name = "badges")
public class Badge extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BadgeCategory category;

    @Column(nullable = false)
    @Builder.Default
    private int threshold = 1;

    @Column(name = "xp_reward", nullable = false)
    @Builder.Default
    private int xpReward = 0;

    @Column(name = "icon_url", nullable = false)
    private String iconUrl;
}
