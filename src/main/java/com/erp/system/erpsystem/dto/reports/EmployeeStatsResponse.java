package com.erp.system.erpsystem.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeStatsResponse {
    private long totalEmployees;
    private double averageAge;
    private double averageTenure;
}
