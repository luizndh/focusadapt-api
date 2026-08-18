package com.focus.adapt.api.application.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.focus.adapt.api.application.dto.BadgeRequest;
import com.focus.adapt.api.application.dto.BadgeResponse;
import com.focus.adapt.api.application.dto.UserAchievementResponse;
import com.focus.adapt.api.domain.entity.Badge;
import com.focus.adapt.api.domain.entity.UserAchievement;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface BadgeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Badge toEntity(BadgeRequest request);

    BadgeResponse toResponse(Badge entity);

    @Mapping(source = "badge.id", target = "badgeId")
    @Mapping(source = "badge.name", target = "badgeName")
    @Mapping(source = "badge.description", target = "badgeDescription")
    @Mapping(source = "badge.iconUrl", target = "badgeIconUrl")
    UserAchievementResponse toResponse(UserAchievement entity);
}
