package com.taskflow.service.impl;

import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.Role;
import com.taskflow.entity.User;
import com.taskflow.exception.UserNotFoundException;
import com.taskflow.mapper.UserMapper;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.CurrentUserService;
import com.taskflow.service.ManagerService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ManagerServiceImpl implements ManagerService {

    private final UserRepository userRepository;

    private final CurrentUserService currentUserService;

    private final UserMapper userMapper;

    // =====================================================
    // GET MY EMPLOYEES
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getMyEmployees() {

        User manager =
                currentUserService.getCurrentUser();

        if (manager.getRole() != Role.MANAGER) {

            throw new AccessDeniedException(
                    "Only managers can access their employees"
            );
        }

        return userRepository
                .findByManagerId(manager.getId())
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // =====================================================
    // ASSIGN EMPLOYEE TO MANAGER
    // =====================================================

    @Override
    public void assignEmployeeToManager(
            Long employeeId,
            Long managerId) {

        User employee =
                userRepository.findById(employeeId)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "Employee not found with id: "
                                                + employeeId
                                )
                        );

        User manager =
                userRepository.findById(managerId)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "Manager not found with id: "
                                                + managerId
                                )
                        );

        // -------------------------------------------------
        // VALIDATE EMPLOYEE
        // -------------------------------------------------

        if (employee.getRole() != Role.EMPLOYEE) {

            throw new IllegalArgumentException(
                    "Selected user is not an employee"
            );
        }

        // -------------------------------------------------
        // VALIDATE MANAGER
        // -------------------------------------------------

        if (manager.getRole() != Role.MANAGER) {

            throw new IllegalArgumentException(
                    "Selected user is not a manager"
            );
        }

        // -------------------------------------------------
        // ACTIVE CHECK
        // -------------------------------------------------

        if (!Boolean.TRUE.equals(employee.getActive())) {

            throw new IllegalArgumentException(
                    "Cannot assign an inactive employee"
            );
        }

        if (!Boolean.TRUE.equals(manager.getActive())) {

            throw new IllegalArgumentException(
                    "Cannot assign employee to an inactive manager"
            );
        }

        // -------------------------------------------------
        // PREVENT SELF ASSIGNMENT
        // -------------------------------------------------

        if (employee.getId().equals(manager.getId())) {

            throw new IllegalArgumentException(
                    "Employee and manager cannot be the same user"
            );
        }

        // -------------------------------------------------
        // ASSIGN
        // -------------------------------------------------

        employee.setManager(manager);

        userRepository.save(employee);
    }

    // =====================================================
    // REMOVE EMPLOYEE FROM MANAGER
    // =====================================================

    @Override
    public void removeEmployeeFromManager(
            Long employeeId) {

        User employee =
                userRepository.findById(employeeId)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "Employee not found with id: "
                                                + employeeId
                                )
                        );

        if (employee.getRole() != Role.EMPLOYEE) {

            throw new IllegalArgumentException(
                    "Selected user is not an employee"
            );
        }

        employee.setManager(null);

        userRepository.save(employee);
    }
}