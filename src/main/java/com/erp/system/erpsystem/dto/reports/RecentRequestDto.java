package com.erp.system.erpsystem.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentRequestDto {
    private String type; // "Leave" or "Permission"
    private String status;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
