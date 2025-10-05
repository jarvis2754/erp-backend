package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.reports.AttendanceOverviewDto;
import com.erp.system.erpsystem.dto.reports.LeavePermissionSummaryDto;
import com.erp.system.erpsystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/attendance")
    public ResponseEntity<AttendanceOverviewDto> getAttendanceOverview(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(reportService.getAttendanceOverview(token));
    }

    @GetMapping("/leaves")
    public ResponseEntity<LeavePermissionSummaryDto> getLeavePermissionSummary(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(reportService.getLeavePermissionSummary(token));
    }
}

