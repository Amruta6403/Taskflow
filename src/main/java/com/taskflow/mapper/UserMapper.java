package com.taskflow.mapper;

import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.User;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .employeeType(user.getEmployeeType())
                .active(user.getActive())

                .managerId(
                        user.getManager() != null
                                ? user.getManager().getId()
                                : null
                )

                .managerName(
                        user.getManager() != null
                                ? user.getManager().getName()
                                : null
                )

                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())

                .build();
    }
}