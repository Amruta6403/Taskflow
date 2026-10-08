package com.taskflow.dto.response;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDateTime dueDate;

    private Long assignedToId;

    private String assignedToName;

    private String assignedToEmail;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}