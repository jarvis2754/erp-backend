package com.erp.system.erpsystem.dto.dashboard;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderStatsDashboardDto {
    private int totalEmployee;
    private int totalPresent;
    private long totalAttendanceEligibleEmployee;
    private Integer pendingApprovals;
    private Integer ActivePOs;

}
