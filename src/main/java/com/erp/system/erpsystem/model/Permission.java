package com.erp.system.erpsystem.model;

import com.erp.system.erpsystem.model.enums.ActionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "permissions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {

//    private int permissionId;
//    private ActionType action;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int permissionId;

    @Enumerated(EnumType.STRING)
    private ActionType action;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

