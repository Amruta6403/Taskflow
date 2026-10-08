package com.taskflow.service.impl;

import com.taskflow.dto.response.AdminDashboardResponse;
import com.taskflow.entity.Role;
import com.taskflow.entity.TaskStatus;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl
        implements AdminDashboardService {

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    @Override
    public AdminDashboardResponse getDashboard() {

        long totalUsers =
                userRepository.count();

        long activeUsers =
                userRepository.countByActiveTrue();

        long inactiveUsers =
                userRepository.countByActiveFalse();

        long totalManagers =
                userRepository.countByRole(
                        Role.MANAGER
                );

        long totalEmployees =
                userRepository.countByRole(
                        Role.EMPLOYEE
                );

        long totalTasks =
                taskRepository.count();

        long pendingTasks =
                taskRepository.countByStatus(
                        TaskStatus.TODO
                );

        long inProgressTasks =
                taskRepository.countByStatus(
                        TaskStatus.IN_PROGRESS
                );

        long completedTasks =
                taskRepository.countByStatus(
                        TaskStatus.COMPLETED
                );

        long cancelledTasks =
                taskRepository.countByStatus(
                        TaskStatus.CANCELLED
                );

        long overdueTasks =
                taskRepository
                        .findAll()
                        .stream()
                        .filter(task ->
                                task.getDueDate() != null
                                && task.getDueDate()
                                        .isBefore(
                                                LocalDateTime.now()
                                        )
                                && task.getStatus()
                                        != TaskStatus.COMPLETED
                                && task.getStatus()
                                        != TaskStatus.CANCELLED
                        )
                        .count();

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .totalManagers(totalManagers)
                .totalEmployees(totalEmployees)
                .totalTasks(totalTasks)
                .pendingTasks(pendingTasks)
                .inProgressTasks(inProgressTasks)
                .completedTasks(completedTasks)
                .cancelledTasks(cancelledTasks)
                .overdueTasks(overdueTasks)
                .build();
    }
}