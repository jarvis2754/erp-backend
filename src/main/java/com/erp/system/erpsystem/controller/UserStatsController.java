package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.reports.CountByCategoryResponse;
import com.erp.system.erpsystem.dto.reports.EmployeeStatsResponse;
import com.erp.system.erpsystem.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hr-report")
@RequiredArgsConstructor
public class UserStatsController {

    private final UserStatsService userStatsService;

    // 1️⃣ Overview: total employees + avg age + avg tenure
    @GetMapping("/overview")
    public EmployeeStatsResponse getEmployeeStats() {
        return userStatsService.getEmployeeStats();
    }

    // 2️⃣ Employee count by tenure (years of service)
    @GetMapping("/tenure")
    public CountByCategoryResponse getCountByTenure() {
        return userStatsService.getCountByTenure();
    }

    // 3️⃣ Employee count by age
    @GetMapping("/age")
    public CountByCategoryResponse getCountByAge() {
        return userStatsService.getCountByAge();
    }

    // 4️⃣ Employee count by position
    @GetMapping("/position")
    public CountByCategoryResponse getCountByPosition() {
        return userStatsService.getCountByPosition();
    }

    // 5️⃣ Employee count by department
    @GetMapping("/department")
    public CountByCategoryResponse getCountByDepartment() {
        return userStatsService.getCountByDepartment();
    }

    // 6️⃣ Gender distribution
    @GetMapping("/gender")
    public CountByCategoryResponse getCountByGender() {
        return userStatsService.getCountByGender();
    }
}

