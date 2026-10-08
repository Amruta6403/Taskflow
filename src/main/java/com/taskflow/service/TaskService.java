package com.taskflow.service;

import com.taskflow.dto.request.TaskRequest;

import com.taskflow.dto.response.TaskResponse;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;

import org.springframework.data.domain.Page;

public interface TaskService {

    TaskResponse createTask(TaskRequest request);

    TaskResponse getTaskById(Long id);

    Page<TaskResponse> searchTasks(
            String keyword,
            TaskStatus status,
            TaskPriority priority,
            Long assignedToId,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    TaskResponse updateTask(
            Long id,
            TaskRequest request
    );
    TaskResponse updateTaskStatus(
            Long id,
            TaskStatus status
    );

    void deleteTask(Long id);
}