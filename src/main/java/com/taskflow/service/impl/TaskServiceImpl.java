package com.taskflow.service.impl;

import com.taskflow.dto.request.TaskRequest;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.entity.Role;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import com.taskflow.entity.User;
import com.taskflow.exception.TaskNotFoundException;
import com.taskflow.exception.UserNotFoundException;
import com.taskflow.mapper.TaskMapper;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.CurrentUserService;
import com.taskflow.service.TaskService;
import com.taskflow.specification.TaskSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final TaskMapper taskMapper;

    private final CurrentUserService currentUserService;

    // =====================================================
    // CREATE TASK
    // =====================================================

    @Override
    public TaskResponse createTask(TaskRequest request) {

        User currentUser =
                currentUserService.getCurrentUser();

        // -------------------------------------------------
        // ONLY ADMIN AND MANAGER CAN CREATE TASKS
        // -------------------------------------------------

        if (currentUser.getRole() == Role.EMPLOYEE) {

            throw new AccessDeniedException(
                    "Employees are not allowed to create tasks"
            );
        }

        // -------------------------------------------------
        // FIND ASSIGNED USER
        // -------------------------------------------------

        User assignedUser =
                findAssignedUser(
                        request.getAssignedToId()
                );

        // -------------------------------------------------
        // VALIDATE ASSIGNMENT
        // -------------------------------------------------

        validateAssignmentPermission(
                currentUser,
                assignedUser
        );

        // -------------------------------------------------
        // DEFAULT STATUS
        // -------------------------------------------------

        TaskStatus status =
                request.getStatus() != null
                        ? request.getStatus()
                        : TaskStatus.TODO;

        // -------------------------------------------------
        // DEFAULT PRIORITY
        // -------------------------------------------------

        TaskPriority priority =
                request.getPriority() != null
                        ? request.getPriority()
                        : TaskPriority.MEDIUM;

        // -------------------------------------------------
        // BUILD TASK
        // -------------------------------------------------

        Task task = Task.builder()

                .title(
                        request.getTitle()
                )

                .description(
                        request.getDescription()
                )

                .status(
                        status
                )

                .priority(
                        priority
                )

                .dueDate(
                        request.getDueDate()
                )

                .assignedTo(
                        assignedUser
                )

                .build();

        // -------------------------------------------------
        // SAVE
        // -------------------------------------------------

        Task savedTask =
                taskRepository.save(task);

        return taskMapper.toResponse(
                savedTask
        );
    }

    // =====================================================
    // GET TASK BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {

        Task task =
                findTask(id);

        User currentUser =
                currentUserService.getCurrentUser();

        if (!canAccessTask(
                currentUser,
                task
        )) {

            throw new AccessDeniedException(
                    "You are not allowed to access this task"
            );
        }

        return taskMapper.toResponse(task);
    }

    // =====================================================
    // SEARCH / FILTER / PAGINATION / SORTING
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> searchTasks(
            String keyword,
            TaskStatus status,
            TaskPriority priority,
            Long assignedToId,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        User currentUser =
                currentUserService.getCurrentUser();

        // -------------------------------------------------
        // PAGE VALIDATION
        // -------------------------------------------------

        if (page < 0) {
            page = 0;
        }

        // -------------------------------------------------
        // SIZE VALIDATION
        // -------------------------------------------------

        if (size <= 0) {
            size = 10;
        }

        if (size > 100) {
            size = 100;
        }

        // -------------------------------------------------
        // SORT FIELD VALIDATION
        // -------------------------------------------------

        if (sortBy == null
                || sortBy.isBlank()
                || !isAllowedSortField(sortBy)) {

            sortBy = "createdAt";
        }

        // -------------------------------------------------
        // SORT DIRECTION
        // -------------------------------------------------

        Sort.Direction direction =
                "desc".equalsIgnoreCase(sortDir)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        // -------------------------------------------------
        // PAGEABLE
        // -------------------------------------------------

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                direction,
                                sortBy
                        )
                );

        // -------------------------------------------------
        // COMMON FILTERS
        // -------------------------------------------------

        Specification<Task> specification =
                Specification
                        .where(
                                TaskSpecification
                                        .hasKeyword(keyword)
                        )
                        .and(
                                TaskSpecification
                                        .hasStatus(status)
                        )
                        .and(
                                TaskSpecification
                                        .hasPriority(priority)
                        );

        // =================================================
        // ADMIN
        // =================================================

        if (currentUser.getRole()
                == Role.ADMIN) {

            if (assignedToId != null) {

                specification =
                        specification.and(
                                TaskSpecification
                                        .assignedToUser(
                                                assignedToId
                                        )
                        );
            }
        }

        // =================================================
        // MANAGER
        // =================================================

        else if (currentUser.getRole()
                == Role.MANAGER) {

            specification =
                    specification.and(
                            TaskSpecification
                                    .assignedToManagerEmployees(
                                            currentUser.getId()
                                    )
                    );

            if (assignedToId != null) {

                specification =
                        specification.and(
                                TaskSpecification
                                        .assignedToUser(
                                                assignedToId
                                        )
                        );
            }
        }

        // =================================================
        // EMPLOYEE
        // =================================================

        else if (currentUser.getRole()
                == Role.EMPLOYEE) {

            specification =
                    specification.and(
                            TaskSpecification
                                    .assignedToUser(
                                            currentUser.getId()
                                    )
                    );
        }

        // -------------------------------------------------
        // DATABASE QUERY
        // -------------------------------------------------

        Page<Task> taskPage =
                taskRepository.findAll(
                        specification,
                        pageable
                );

        // -------------------------------------------------
        // ENTITY -> DTO
        // -------------------------------------------------

        return taskPage.map(
                taskMapper::toResponse
        );
    }

    // =====================================================
    // UPDATE TASK
    // =====================================================

    @Override
    public TaskResponse updateTask(
            Long id,
            TaskRequest request) {

        Task task =
                findTask(id);

        User currentUser =
                currentUserService.getCurrentUser();

        // =================================================
        // EMPLOYEE
        // =================================================

        /*
         * Employees must use the dedicated
         * updateTaskStatus() API.
         *
         * This prevents employees from changing:
         *
         * - title
         * - description
         * - priority
         * - due date
         * - assigned employee
         */

        if (currentUser.getRole()
                == Role.EMPLOYEE) {

            throw new AccessDeniedException(
                    "Employees can update task status only"
            );
        }

        // =================================================
        // CHECK EXISTING TASK ACCESS
        // =================================================

        if (!canAccessTask(
                currentUser,
                task
        )) {

            throw new AccessDeniedException(
                    "You are not allowed to update this task"
            );
        }

        // =================================================
        // FIND ASSIGNED USER
        // =================================================

        User assignedUser =
                findAssignedUser(
                        request.getAssignedToId()
                );

        // =================================================
        // VALIDATE ASSIGNMENT
        // =================================================

        validateAssignmentPermission(
                currentUser,
                assignedUser
        );

        // =================================================
        // UPDATE BASIC INFORMATION
        // =================================================

        task.setTitle(
                request.getTitle()
        );

        task.setDescription(
                request.getDescription()
        );

        // =================================================
        // STATUS
        // =================================================

        if (request.getStatus() != null) {

            task.setStatus(
                    request.getStatus()
            );
        }

        // =================================================
        // PRIORITY
        // =================================================

        if (request.getPriority() != null) {

            task.setPriority(
                    request.getPriority()
            );
        }

        // =================================================
        // DUE DATE
        // =================================================

        task.setDueDate(
                request.getDueDate()
        );

        // =================================================
        // ASSIGNMENT
        // =================================================

        task.setAssignedTo(
                assignedUser
        );

        // =================================================
        // SAVE
        // =================================================

        Task updatedTask =
                taskRepository.save(task);

        return taskMapper.toResponse(
                updatedTask
        );
    }

    // =====================================================
    // UPDATE TASK STATUS
    // =====================================================

    @Override
    public TaskResponse updateTaskStatus(
            Long id,
            TaskStatus status) {

        // -------------------------------------------------
        // VALIDATE STATUS
        // -------------------------------------------------

        if (status == null) {

            throw new IllegalArgumentException(
                    "Task status cannot be null"
            );
        }

        // -------------------------------------------------
        // FIND TASK
        // -------------------------------------------------

        Task task =
                findTask(id);

        // -------------------------------------------------
        // CURRENT USER
        // -------------------------------------------------

        User currentUser =
                currentUserService.getCurrentUser();

        // -------------------------------------------------
        // ACCESS CHECK
        // -------------------------------------------------

        if (!canAccessTask(
                currentUser,
                task
        )) {

            throw new AccessDeniedException(
                    "You are not allowed to update this task"
            );
        }

        // -------------------------------------------------
        // UPDATE STATUS
        // -------------------------------------------------

        task.setStatus(
                status
        );

        // -------------------------------------------------
        // SAVE
        // -------------------------------------------------

        Task updatedTask =
                taskRepository.save(task);

        return taskMapper.toResponse(
                updatedTask
        );
    }

    // =====================================================
    // DELETE TASK
    // =====================================================

    @Override
    public void deleteTask(Long id) {

        Task task =
                findTask(id);

        User currentUser =
                currentUserService.getCurrentUser();

        // -------------------------------------------------
        // EMPLOYEE CANNOT DELETE
        // -------------------------------------------------

        if (currentUser.getRole()
                == Role.EMPLOYEE) {

            throw new AccessDeniedException(
                    "Employees are not allowed to delete tasks"
            );
        }

        // -------------------------------------------------
        // ACCESS CHECK
        // -------------------------------------------------

        if (!canAccessTask(
                currentUser,
                task
        )) {

            throw new AccessDeniedException(
                    "You are not allowed to delete this task"
            );
        }

        // -------------------------------------------------
        // DELETE
        // -------------------------------------------------

        taskRepository.delete(task);
    }

    // =====================================================
    // FIND TASK
    // =====================================================

    private Task findTask(Long id) {

        return taskRepository
                .findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "Task not found with id: "
                                        + id
                        )
                );
    }

    // =====================================================
    // FIND ASSIGNED USER
    // =====================================================

    private User findAssignedUser(
            Long assignedToId) {

        if (assignedToId == null) {

            throw new UserNotFoundException(
                    "Assigned user is required"
            );
        }

        return userRepository
                .findById(assignedToId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: "
                                        + assignedToId
                        )
                );
    }

    // =====================================================
    // VALIDATE TASK ASSIGNMENT
    // =====================================================

    private void validateAssignmentPermission(
            User currentUser,
            User assignedUser) {

        // =================================================
        // ADMIN
        // =================================================

        if (currentUser.getRole()
                == Role.ADMIN) {

            if (!Boolean.TRUE.equals(
                    assignedUser.getActive()
            )) {

                throw new AccessDeniedException(
                        "Cannot assign task to an inactive user"
                );
            }

            return;
        }

        // =================================================
        // MANAGER
        // =================================================

        if (currentUser.getRole()
                == Role.MANAGER) {

            if (!Boolean.TRUE.equals(
                    assignedUser.getActive()
            )) {

                throw new AccessDeniedException(
                        "Cannot assign task to an inactive user"
                );
            }

            if (assignedUser.getRole()
                    != Role.EMPLOYEE) {

                throw new AccessDeniedException(
                        "Managers can assign tasks only to employees"
                );
            }

            if (!isEmployeeOfManager(
                    assignedUser,
                    currentUser
            )) {

                throw new AccessDeniedException(
                        "You can assign tasks only to your employees"
                );
            }

            return;
        }

        // =================================================
        // EMPLOYEE
        // =================================================

        if (currentUser.getRole()
                == Role.EMPLOYEE) {

            throw new AccessDeniedException(
                    "Employees are not allowed to assign tasks"
            );
        }

        throw new AccessDeniedException(
                "You are not allowed to assign tasks"
        );
    }

    // =====================================================
    // CHECK EMPLOYEE-MANAGER RELATIONSHIP
    // =====================================================

    private boolean isEmployeeOfManager(
            User employee,
            User manager) {

        if (employee.getManager() == null) {
            return false;
        }

        return employee.getManager()
                .getId()
                .equals(manager.getId());
    }

    // =====================================================
    // CHECK TASK ACCESS
    // =====================================================

    private boolean canAccessTask(
            User currentUser,
            Task task) {

        // =================================================
        // ADMIN
        // =================================================

        if (currentUser.getRole()
                == Role.ADMIN) {

            return true;
        }

        // =================================================
        // TASK MUST HAVE ASSIGNEE
        // =================================================

        if (task.getAssignedTo() == null) {
            return false;
        }

        User assignedUser =
                task.getAssignedTo();

        // =================================================
        // MANAGER
        // =================================================

        if (currentUser.getRole()
                == Role.MANAGER) {

            return isEmployeeOfManager(
                    assignedUser,
                    currentUser
            );
        }

        // =================================================
        // EMPLOYEE
        // =================================================

        if (currentUser.getRole()
                == Role.EMPLOYEE) {

            return assignedUser.getId()
                    .equals(currentUser.getId());
        }

        return false;
    }

    // =====================================================
    // ALLOWED SORT FIELDS
    // =====================================================

    private boolean isAllowedSortField(
            String sortBy) {

        return switch (sortBy) {

            case "id",
                 "title",
                 "status",
                 "priority",
                 "dueDate",
                 "createdAt",
                 "updatedAt" -> true;

            default -> false;
        };
    }
}