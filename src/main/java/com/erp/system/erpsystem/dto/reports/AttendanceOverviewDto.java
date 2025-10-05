package com.erp.system.erpsystem.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
public class AttendanceOverviewDto {
    private long daysPresent;
    private long daysAbsent;
    private long lateArrivals;
    private double averageWorkHours;
    private Map<LocalDate, String> attendanceByDay; // date -> status
    private Map<LocalDate, Double> workHoursTrend; // date -> hours
}
