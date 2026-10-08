package com.taskflow.service.impl;

import com.taskflow.dto.request.UserRequest;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.Role;
import com.taskflow.entity.TaskStatus;
import com.taskflow.entity.User;
import com.taskflow.exception.UserNotFoundException;
import com.taskflow.mapper.UserMapper;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.CurrentUserService;
import com.taskflow.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final CurrentUserService currentUserService;

    private final TaskRepository taskRepository;


    // =====================================================
    // CREATE USER
    // =====================================================

    @Override
    public UserResponse createUser(UserRequest request) {

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only ADMIN can create users"
            );
        }

        if (userRepository.existsByEmail(
                request.getEmail())) {

            throw new IllegalArgumentException(
                    "Email already exists: "
                            + request.getEmail()
            );
        }

        validateUserData(request);

        User manager = null;

        if (request.getRole() == Role.EMPLOYEE) {

            manager = findUser(
                    request.getManagerId()
            );

            validateManager(manager);
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .role(request.getRole())
                .employeeType(
                        request.getRole() == Role.EMPLOYEE
                                ? request.getEmployeeType()
                                : null
                )
                .manager(manager)
                .active(true)
                .build();

        User savedUser =
                userRepository.save(user);

        return buildUserResponse(savedUser);
    }


    // =====================================================
    // GET USER BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User currentUser =
                currentUserService.getCurrentUser();

        User user = findUser(id);

        if (currentUser.getRole() == Role.ADMIN) {

            return buildUserResponse(user);
        }

        if (currentUser.getRole() == Role.MANAGER) {

            if (isEmployeeOfManager(
                    user,
                    currentUser)) {

                return buildUserResponse(user);
            }

            throw new AccessDeniedException(
                    "You can only view your employees"
            );
        }

        throw new AccessDeniedException(
                "You are not allowed to view users"
        );
    }


    // =====================================================
    // GET ALL USERS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only ADMIN can view all users"
            );
        }

        return userRepository.findAll()
                .stream()
                .map(this::buildUserResponse)
                .toList();
    }


    // =====================================================
    // GET USERS BY ROLE
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(
            Role role) {

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only ADMIN can filter users by role"
            );
        }

        if (role == null) {

            throw new IllegalArgumentException(
                    "Role is required"
            );
        }

        return userRepository
                .findByRole(role)
                .stream()
                .map(this::buildUserResponse)
                .toList();
    }


    // =====================================================
    // GET MY EMPLOYEES
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getMyEmployees() {

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole() != Role.MANAGER) {

            throw new AccessDeniedException(
                    "Only MANAGER can view their employees"
            );
        }

        return userRepository
                .findByManagerId(
                        currentUser.getId()
                )
                .stream()
                .map(this::buildUserResponse)
                .toList();
    }


    // =====================================================
    // UPDATE USER
    // =====================================================

    @Override
    public UserResponse updateUser(
            Long id,
            UserRequest request) {

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only ADMIN can update users"
            );
        }

        User user = findUser(id);

        if (!user.getEmail()
                .equalsIgnoreCase(
                        request.getEmail()
                )) {

            if (userRepository.existsByEmail(
                    request.getEmail()
            )) {

                throw new IllegalArgumentException(
                        "Email already exists: "
                                + request.getEmail()
                );
            }
        }

        validateUserData(request);

        User manager = null;

        if (request.getRole() == Role.EMPLOYEE) {

            if (request.getManagerId() == null) {

                throw new IllegalArgumentException(
                        "Manager is required for employees"
                );
            }

            if (id.equals(
                    request.getManagerId()
            )) {

                throw new IllegalArgumentException(
                        "User cannot be their own manager"
                );
            }

            manager = findUser(
                    request.getManagerId()
            );

            validateManager(manager);
        }

        user.setName(
                request.getName().trim()
        );

        user.setEmail(
                request.getEmail().trim()
        );

        user.setRole(
                request.getRole()
        );

        if (request.getRole() == Role.EMPLOYEE) {

            user.setEmployeeType(
                    request.getEmployeeType()
            );

        } else {

            user.setEmployeeType(null);
        }

        user.setManager(manager);

        if (request.getPassword() != null
                && !request.getPassword()
                        .trim()
                        .isEmpty()) {

            user.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );
        }

        User updatedUser =
                userRepository.save(user);

        return buildUserResponse(updatedUser);
    }


    // =====================================================
    // ASSIGN EMPLOYEE TO MANAGER
    // =====================================================

    @Override
    public UserResponse assignEmployeeToManager(
            Long employeeId,
            Long managerId) {

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only ADMIN can assign employees to managers"
            );
        }

        User employee =
                findUser(employeeId);

        User manager =
                findUser(managerId);

        if (employee.getRole()
                != Role.EMPLOYEE) {

            throw new IllegalArgumentException(
                    "Selected user is not an employee"
            );
        }

        validateManager(manager);

        if (employee.getId()
                .equals(manager.getId())) {

            throw new IllegalArgumentException(
                    "Employee and manager cannot be the same user"
            );
        }

        employee.setManager(manager);

        User savedEmployee =
                userRepository.save(employee);

        return buildUserResponse(savedEmployee);
    }


    // =====================================================
    // ACTIVATE USER
    // =====================================================

    @Override
    public UserResponse activateUser(Long id) {

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole()
                != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only ADMIN can activate users"
            );
        }

        User user = findUser(id);

        user.setActive(true);

        User savedUser =
                userRepository.save(user);

        return buildUserResponse(savedUser);
    }


    // =====================================================
    // DEACTIVATE USER
    // =====================================================

    @Override
    public UserResponse deactivateUser(Long id) {

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole()
                != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only ADMIN can deactivate users"
            );
        }

        User user = findUser(id);

        if (user.getId()
                .equals(currentUser.getId())) {

            throw new IllegalArgumentException(
                    "You cannot deactivate your own account"
            );
        }

        user.setActive(false);

        User savedUser =
                userRepository.save(user);

        return buildUserResponse(savedUser);
    }


    // =====================================================
    // DELETE USER
    // =====================================================

    @Override
    public void deleteUser(Long id) {

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getRole()
                != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only ADMIN can delete users"
            );
        }

        User user = findUser(id);

        if (user.getId()
                .equals(currentUser.getId())) {

            throw new IllegalArgumentException(
                    "You cannot delete your own account"
            );
        }

        if (user.getRole()
                == Role.ADMIN) {

            throw new IllegalArgumentException(
                    "Admin users cannot be deleted"
            );
        }

        if (user.getRole()
                == Role.MANAGER) {

            if (userRepository
                    .existsByManagerId(id)) {

                throw new IllegalArgumentException(
                        "Cannot delete manager. "
                                + "Reassign employees first."
                );
            }
        }

        userRepository.delete(user);
    }


    // =====================================================
    // BUILD USER RESPONSE
    // =====================================================

    private UserResponse buildUserResponse(
            User user) {

        UserResponse response =
                userMapper.toResponse(user);

        if (response == null) {
            return null;
        }

        Long userId = user.getId();

        // -------------------------------------------------
        // TOTAL
        // -------------------------------------------------

        long totalTasks =
                taskRepository
                        .countByAssignedToId(userId);

        // -------------------------------------------------
        // COMPLETED
        // -------------------------------------------------

        long completedTasks =
                taskRepository
                        .countByAssignedToIdAndStatus(
                                userId,
                                TaskStatus.COMPLETED
                        );

        // -------------------------------------------------
        // IN PROGRESS
        // -------------------------------------------------

        long inProgressTasks =
                taskRepository
                        .countByAssignedToIdAndStatus(
                                userId,
                                TaskStatus.IN_PROGRESS
                        );

        // -------------------------------------------------
        // TODO / PENDING
        // -------------------------------------------------

        long pendingTasks =
                taskRepository
                        .countByAssignedToIdAndStatus(
                                userId,
                                TaskStatus.TODO
                        );

        // -------------------------------------------------
        // OVERDUE
        // -------------------------------------------------

        long overdueTasks = 0;

        if (totalTasks > 0) {

            overdueTasks =
                    taskRepository
                            .countByAssignedToIdAndDueDateBefore(
                                    userId,
                                    LocalDateTime.now()
                            );

            // Completed tasks should not be displayed
            // as overdue.

            overdueTasks -= completedTasks;

            if (overdueTasks < 0) {
                overdueTasks = 0;
            }
        }

        // -------------------------------------------------
        // COMPLETION %
        // -------------------------------------------------

        int completionPercentage = 0;

        if (totalTasks > 0) {

            completionPercentage =
                    (int) Math.round(
                            ((double) completedTasks
                                    / totalTasks)
                                    * 100
                    );
        }

        // -------------------------------------------------
        // SET RESPONSE
        // -------------------------------------------------

        response.setTotalTasks(totalTasks);

        response.setCompletedTasks(
                completedTasks
        );

        response.setInProgressTasks(
                inProgressTasks
        );

        response.setPendingTasks(
                pendingTasks
        );

        response.setOverdueTasks(
                overdueTasks
        );

        response.setCompletionPercentage(
                completionPercentage
        );

        return response;
    }


    // =====================================================
    // VALIDATE USER DATA
    // =====================================================

    private void validateUserData(
            UserRequest request) {

        Role role = request.getRole();

        if (role == null) {

            throw new IllegalArgumentException(
                    "Role is required"
            );
        }

        if (role == Role.ADMIN) {

            if (request.getEmployeeType()
                    != null) {

                throw new IllegalArgumentException(
                        "Admin cannot have an employee type"
                );
            }

            if (request.getManagerId()
                    != null) {

                throw new IllegalArgumentException(
                        "Admin cannot have a manager"
                );
            }
        }

        else if (role == Role.MANAGER) {

            if (request.getEmployeeType()
                    != null) {

                throw new IllegalArgumentException(
                        "Manager cannot have an employee type"
                );
            }

            if (request.getManagerId()
                    != null) {

                throw new IllegalArgumentException(
                        "Manager cannot have a manager"
                );
            }
        }

        else if (role == Role.EMPLOYEE) {

            if (request.getEmployeeType()
                    == null) {

                throw new IllegalArgumentException(
                        "Employee type is required for employees"
                );
            }

            if (request.getManagerId()
                    == null) {

                throw new IllegalArgumentException(
                        "Manager is required for employees"
                );
            }
        }
    }


    // =====================================================
    // FIND USER
    // =====================================================

    private User findUser(Long id) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "User id is required"
            );
        }

        return userRepository
                .findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: "
                                        + id
                        )
                );
    }


    // =====================================================
    // VALIDATE MANAGER
    // =====================================================

    private void validateManager(
            User manager) {

        if (manager.getRole()
                != Role.MANAGER) {

            throw new IllegalArgumentException(
                    "Selected user is not a manager"
            );
        }

        if (!Boolean.TRUE.equals(
                manager.getActive())) {

            throw new IllegalArgumentException(
                    "Selected manager is inactive"
            );
        }
    }


    // =====================================================
    // MANAGER RELATIONSHIP
    // =====================================================

    private boolean isEmployeeOfManager(
            User employee,
            User manager) {

        return employee.getManager() != null
                && employee.getManager()
                        .getId()
                        .equals(
                                manager.getId()
                        );
    }
}