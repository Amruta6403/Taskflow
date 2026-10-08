package com.taskflow.dto.request;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskFilterRequest {

    private String search;

    private TaskStatus status;

    private TaskPriority priority;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String direction = "desc";
}