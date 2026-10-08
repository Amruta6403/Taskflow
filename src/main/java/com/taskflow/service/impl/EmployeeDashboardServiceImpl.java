package com.taskflow.service.impl;

import com.taskflow.dto.response.EmployeeDashboardResponse;
import com.taskflow.entity.Role;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import com.taskflow.entity.User;
import com.taskflow.repository.TaskRepository;
import com.taskflow.security.CurrentUserService;
import com.taskflow.service.EmployeeDashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeDashboardServiceImpl
        implements EmployeeDashboardService {

    private final TaskRepository taskRepository;

    private final CurrentUserService currentUserService;

    // =====================================================
    // EMPLOYEE DASHBOARD
    // =====================================================

    @Override
    public EmployeeDashboardResponse getDashboard() {

        // =================================================
        // CURRENT USER
        // =================================================

        User currentUser =
                currentUserService.getCurrentUser();

        // =================================================
        // ROLE CHECK
        // =================================================

        if (currentUser.getRole() != Role.EMPLOYEE) {

            throw new IllegalStateException(
                    "Only employees can access employee dashboard"
            );
        }

        // =================================================
        // GET MY TASKS
        // =================================================

        List<Task> tasks =
                taskRepository.findByAssignedToId(
                        currentUser.getId()
                );

        // =================================================
        // COUNTERS
        // =================================================

        long totalTasks =
                tasks.size();

        long todoTasks = 0;

        long inProgressTasks = 0;

        long completedTasks = 0;

        long cancelledTasks = 0;

        long overdueTasks = 0;

        long lowPriorityTasks = 0;

        long mediumPriorityTasks = 0;

        long highPriorityTasks = 0;

        LocalDateTime now =
                LocalDateTime.now();

        // =================================================
        // PROCESS TASKS
        // =================================================

        for (Task task : tasks) {

            // =============================================
            // STATUS
            // =============================================

            if (task.getStatus() == TaskStatus.TODO) {

                todoTasks++;

            } else if (
                    task.getStatus()
                            == TaskStatus.IN_PROGRESS) {

                inProgressTasks++;

            } else if (
                    task.getStatus()
                            == TaskStatus.COMPLETED) {

                completedTasks++;

            } else if (
                    task.getStatus()
                            == TaskStatus.CANCELLED) {

                cancelledTasks++;
            }

            // =============================================
            // OVERDUE
            // =============================================

            if (task.getDueDate() != null
                    && task.getDueDate().isBefore(now)
                    && task.getStatus()
                            != TaskStatus.COMPLETED
                    && task.getStatus()
                            != TaskStatus.CANCELLED) {

                overdueTasks++;
            }

            // =============================================
            // PRIORITY
            // =============================================

            if (task.getPriority()
                    == TaskPriority.LOW) {

                lowPriorityTasks++;

            } else if (
                    task.getPriority()
                            == TaskPriority.MEDIUM) {

                mediumPriorityTasks++;

            } else if (
                    task.getPriority()
                            == TaskPriority.HIGH) {

                highPriorityTasks++;
            }
        }

        // =================================================
        // RESPONSE
        // =================================================

        return EmployeeDashboardResponse.builder()

                .totalTasks(totalTasks)

                .todoTasks(todoTasks)

                .inProgressTasks(inProgressTasks)

                .completedTasks(completedTasks)

                .cancelledTasks(cancelledTasks)

                .overdueTasks(overdueTasks)

                .lowPriorityTasks(lowPriorityTasks)

                .mediumPriorityTasks(mediumPriorityTasks)

                .highPriorityTasks(highPriorityTasks)

                .build();
    }
}