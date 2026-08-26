package com.focus.adapt.api.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.focus.adapt.api.application.dto.ContentBlockResponse;
import com.focus.adapt.api.domain.entity.ContentBlock;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContentBlockMapper {

    @Mapping(source = "sortOrder", target = "order")
    @Mapping(source = "blockType", target = "type")
    @Mapping(source = "studyTask.id", target = "studyTaskId")
    ContentBlockResponse toResponse(ContentBlock entity);
}
