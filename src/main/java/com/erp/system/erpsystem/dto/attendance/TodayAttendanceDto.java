package com.erp.system.erpsystem.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class TodayAttendanceDto {
    private String status; // Not Checked In, Checked In, Completed
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
}
