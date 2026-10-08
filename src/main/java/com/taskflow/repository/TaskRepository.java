package com.taskflow.repository;

import com.taskflow.entity.Task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository
        extends JpaRepository<Task, Long>,
                JpaSpecificationExecutor<Task> {

    // =====================================================
    // ASSIGNED USER
    // =====================================================

    List<Task> findByAssignedToId(Long userId);

    // =====================================================
    // STATUS
    // =====================================================

    long countByStatus(
            com.taskflow.entity.TaskStatus status
    );

    // =====================================================
    // USER + STATUS
    // =====================================================

    long countByAssignedToIdAndStatus(
            Long userId,
            com.taskflow.entity.TaskStatus status
    );

    // =====================================================
    // USER TASKS
    // =====================================================

    long countByAssignedToId(
            Long userId
    );

    // =====================================================
    // DUE DATE
    // =====================================================

    long countByAssignedToIdAndDueDateBefore(
            Long userId,
            LocalDateTime date
    );

    // =====================================================
    // DUE TODAY / RANGE
    // =====================================================

    long countByAssignedToIdAndDueDateGreaterThanEqualAndDueDateLessThan(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    // =====================================================
    // TEAM TASKS
    // =====================================================

    long countByAssignedToManagerId(
            Long managerId
    );
}