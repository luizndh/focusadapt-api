package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfileType;
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
public class ProcrastinationProfileResponse {
    private ProcrastinationProfileType profileType;
    private String description;
    private String characteristics;
    private String tips;
}
