package com.taskflow.mapper;

import com.taskflow.dto.response.TaskResponse;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;

import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {

        if (task == null) {
            return null;
        }

        User assignedTo = task.getAssignedTo();

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .assignedToId(
                        assignedTo != null
                                ? assignedTo.getId()
                                : null
                )
                .assignedToName(
                        assignedTo != null
                                ? assignedTo.getName()
                                : null
                )
                .assignedToEmail(
                        assignedTo != null
                                ? assignedTo.getEmail()
                                : null
                )
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}