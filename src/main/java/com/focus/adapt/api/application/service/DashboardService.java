package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.dto.DashboardResponse;
import com.focus.adapt.api.domain.entity.User;

public interface DashboardService {
    DashboardResponse getDashboard(User user);
}
