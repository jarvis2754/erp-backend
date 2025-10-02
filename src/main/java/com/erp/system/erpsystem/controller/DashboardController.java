package com.erp.system.erpsystem.controller;


import com.erp.system.erpsystem.dto.dashboard.AttendanceTrendDashboardDto;
import com.erp.system.erpsystem.dto.dashboard.LeaveStatsDashboardDto;
import com.erp.system.erpsystem.dto.dashboard.OrderStatsDashboardDto;
import com.erp.system.erpsystem.service.DashboardService;

import com.erp.system.erpsystem.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // ✅ Attendance Trends
    @GetMapping("/attendance-trends")
    public ResponseEntity<?> getAttendanceTrends(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            List<AttendanceTrendDashboardDto> response = dashboardService.getAttendanceTrend(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to load attendance trends");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Leave Status
    @GetMapping("/leave-status")
    public ResponseEntity<?> getLeaveStatus(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            LeaveStatsDashboardDto response = dashboardService.getLeaveStats(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to load leave status");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Order / Vendor Stats
    @GetMapping("/order-stats")
    public ResponseEntity<?> getOrderStats(@RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7);
            OrderStatsDashboardDto response = dashboardService.getOrderStats(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to load order stats");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


}
