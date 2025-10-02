package com.erp.system.erpsystem.dto.dashboard;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttendanceTrendDashboardDto {
    private LocalDateTime date;
    private int attendanceCount;
    private long totalEmployee;
}
