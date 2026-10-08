package com.taskflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_email",
                        columnNames = "email"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // BASIC INFORMATION
    // =====================================================

    @Column(nullable = false, length = 100)
    private String name;

    @Column(
            nullable = false,
            unique = true,
            length = 150
    )
    private String email;

    @Column(nullable = false)
    private String password;

    // =====================================================
    // ROLE
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private Role role;

    // =====================================================
    // EMPLOYEE TYPE
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private EmployeeType employeeType;

    // =====================================================
    // ACTIVE STATUS
    // =====================================================

    @Column(
            nullable = false
    )
    @Builder.Default
    private Boolean active = true;

    // =====================================================
    // MANAGER
    // =====================================================

    /*
     * Employee -> Manager
     *
     * EMPLOYEE
     *      |
     *      +---- manager ----> MANAGER
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "manager_id"
    )
    private User manager;

    // =====================================================
    // EMPLOYEES
    // =====================================================

    /*
     * One manager can have many employees.
     *
     * This is the inverse side of manager.
     */

    @OneToMany(
            mappedBy = "manager",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<User> employees =
            new ArrayList<>();

    // =====================================================
    // TIMESTAMPS
    // =====================================================

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // =====================================================
    // PRE-PERSIST
    // =====================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (active == null) {
            active = true;
        }
    }

    // =====================================================
    // PRE-UPDATE
    // =====================================================

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}