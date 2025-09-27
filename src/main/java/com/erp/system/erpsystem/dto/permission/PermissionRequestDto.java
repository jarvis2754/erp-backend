package com.erp.system.erpsystem.dto.permission;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionRequestDto {

    private Integer permissionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer requestedById;
    private String requestedByName;

    private Integer approvedById;
    private String approvedByName;

    private Integer orgId;
    private String orgName;
}

