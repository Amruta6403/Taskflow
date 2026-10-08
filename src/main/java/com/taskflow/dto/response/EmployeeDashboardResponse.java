package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDashboardResponse {

    // =====================================================
    // TASK STATISTICS
    // =====================================================

    private long totalTasks;

    private long todoTasks;

    private long inProgressTasks;

    private long completedTasks;

    private long cancelledTasks;

    private long overdueTasks;

    // =====================================================
    // PRIORITY STATISTICS
    // =====================================================

    private long lowPriorityTasks;

    private long mediumPriorityTasks;

    private long highPriorityTasks;
}