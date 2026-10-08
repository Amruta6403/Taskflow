package com.taskflow.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeeTaskStatsResponse {

    private long totalTasks;

    private long completedTasks;

    private long inProgressTasks;

    private long pendingTasks;

    private long overdueTasks;

    private double completionPercentage;
}