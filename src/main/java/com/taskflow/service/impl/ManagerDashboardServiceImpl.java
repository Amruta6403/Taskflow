package com.taskflow.service.impl;

import com.taskflow.dto.response.ManagerDashboardResponse;
import com.taskflow.entity.Role;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import com.taskflow.entity.User;
import com.taskflow.repository.TaskRepository;
import com.taskflow.security.CurrentUserService;
import com.taskflow.service.ManagerDashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import com.taskflow.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerDashboardServiceImpl
        implements ManagerDashboardService {

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    private final CurrentUserService currentUserService;

    @Override
    public ManagerDashboardResponse getDashboard() {

        User manager =
                currentUserService.getCurrentUser();

        if (manager.getRole() != Role.MANAGER) {

            throw new IllegalStateException(
                    "Only managers can access manager dashboard"
            );
        }

        List<User> employees =
                userRepository.findByManagerId(
                        manager.getId()
                );

        long totalEmployees =
                employees.size();

        long activeEmployees =
                employees.stream()
                        .filter(employee ->
                                Boolean.TRUE.equals(
                                        employee.getActive()
                                )
                        )
                        .count();

        long inactiveEmployees =
                employees.stream()
                        .filter(employee ->
                                !Boolean.TRUE.equals(
                                        employee.getActive()
                                )
                        )
                        .count();

        long totalTasks = 0;
        long todoTasks = 0;
        long inProgressTasks = 0;
        long completedTasks = 0;
        long cancelledTasks = 0;
        long overdueTasks = 0;

        long lowPriorityTasks = 0;
        long mediumPriorityTasks = 0;
        long highPriorityTasks = 0;

        // IMPORTANT:
        // Task.dueDate is LocalDateTime
        LocalDateTime now = LocalDateTime.now();

        for (User employee : employees) {

            List<Task> employeeTasks =
                    taskRepository.findByAssignedToId(
                            employee.getId()
                    );

            totalTasks += employeeTasks.size();

            for (Task task : employeeTasks) {

                // =========================================
                // STATUS
                // =========================================

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

                // =========================================
                // OVERDUE
                // =========================================

                if (task.getDueDate() != null
                        && task.getDueDate().isBefore(now)
                        && task.getStatus()
                                != TaskStatus.COMPLETED
                        && task.getStatus()
                                != TaskStatus.CANCELLED) {

                    overdueTasks++;
                }

                // =========================================
                // PRIORITY
                // =========================================

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
        }

        return ManagerDashboardResponse.builder()

                .totalEmployees(totalEmployees)

                .activeEmployees(activeEmployees)

                .inactiveEmployees(inactiveEmployees)

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