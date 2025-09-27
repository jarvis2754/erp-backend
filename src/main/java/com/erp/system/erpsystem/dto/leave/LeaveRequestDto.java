package com.erp.system.erpsystem.dto.leave;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDto {

    private Integer leaveId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
    private String leaveType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer requestedById;
    private String requestedByName;

    private Integer approvedById;
    private String approvedByName;

    private Integer orgId;
    private String orgName;
}

