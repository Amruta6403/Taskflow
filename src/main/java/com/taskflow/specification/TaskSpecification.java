package com.taskflow.specification;

import com.taskflow.entity.Task;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecification {

    private TaskSpecification() {
    }

    // =====================================================
    // KEYWORD
    // =====================================================

    public static Specification<Task> hasKeyword(
            String keyword) {

        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String value =
                    "%" + keyword.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(
                            cb.lower(root.get("title")),
                            value
                    ),
                    cb.like(
                            cb.lower(root.get("description")),
                            value
                    )
            );
        };
    }

    // =====================================================
    // STATUS
    // =====================================================

    public static Specification<Task> hasStatus(
            TaskStatus status) {

        return (root, query, cb) -> {

            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("status"),
                    status
            );
        };
    }

    // =====================================================
    // PRIORITY
    // =====================================================

    public static Specification<Task> hasPriority(
            TaskPriority priority) {

        return (root, query, cb) -> {

            if (priority == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("priority"),
                    priority
            );
        };
    }

    // =====================================================
    // ASSIGNED USER
    // =====================================================

    public static Specification<Task> assignedToUser(
            Long userId) {

        return (root, query, cb) -> {

            if (userId == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("assignedTo").get("id"),
                    userId
            );
        };
    }

    // =====================================================
    // MANAGER → EMPLOYEE TASKS
    // =====================================================

    public static Specification<Task> assignedToManagerEmployees(
            Long managerId) {

        return (root, query, cb) -> {

            if (managerId == null) {
                return cb.disjunction();
            }

            Join<Task, com.taskflow.entity.User> assignedUser =
                    root.join(
                            "assignedTo",
                            JoinType.INNER
                    );

            return cb.equal(
                    assignedUser
                            .get("manager")
                            .get("id"),
                    managerId
            );
        };
    }
}