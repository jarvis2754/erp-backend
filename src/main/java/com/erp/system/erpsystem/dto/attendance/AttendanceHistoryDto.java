package com.erp.system.erpsystem.dto.attendance;

import com.erp.system.erpsystem.model.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class AttendanceHistoryDto {
    private LocalDate date;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private double workHours; // e.g., 8.75
    private AttendanceStatus status; // Present, Absent, Leave
}