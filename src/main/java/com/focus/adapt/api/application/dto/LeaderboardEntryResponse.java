package com.focus.adapt.api.application.dto;

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
public class LeaderboardEntryResponse {
    private int rank;
    private String userName;
    private int level;
    private int xpPoints;
    private boolean isCurrentUser;
}
