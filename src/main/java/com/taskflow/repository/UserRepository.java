package com.taskflow.repository;

import com.taskflow.entity.Role;
import com.taskflow.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long>,
                JpaSpecificationExecutor<User> {

    // =====================================================
    // FIND BY EMAIL
    // =====================================================

    Optional<User> findByEmail(String email);

    // =====================================================
    // CHECK EMAIL
    // =====================================================

    boolean existsByEmail(String email);

    // =====================================================
    // FIND USERS BY ROLE
    // =====================================================

    List<User> findByRole(Role role);

    // =====================================================
    // FIND ACTIVE USERS BY ROLE
    // =====================================================

    List<User> findByRoleAndActiveTrue(Role role);

    // =====================================================
    // FIND EMPLOYEES OF MANAGER
    // =====================================================

    List<User> findByManagerId(Long managerId);

    // =====================================================
    // FIND ACTIVE EMPLOYEES OF MANAGER
    // =====================================================

    List<User> findByManagerIdAndActiveTrue(
            Long managerId
    );

    // =====================================================
    // CHECK WHETHER MANAGER HAS EMPLOYEES
    // =====================================================

    boolean existsByManagerId(Long managerId);

    // =====================================================
    // COUNT BY ROLE
    // =====================================================

    long countByRole(Role role);

    // =====================================================
    // COUNT ACTIVE USERS BY ROLE
    // =====================================================

    long countByRoleAndActiveTrue(Role role);

    // =====================================================
    // COUNT INACTIVE USERS BY ROLE
    // =====================================================

    long countByRoleAndActiveFalse(Role role);

    // =====================================================
    // COUNT ACTIVE USERS
    // =====================================================

    long countByActiveTrue();

    // =====================================================
    // COUNT INACTIVE USERS
    // =====================================================

    long countByActiveFalse();
}