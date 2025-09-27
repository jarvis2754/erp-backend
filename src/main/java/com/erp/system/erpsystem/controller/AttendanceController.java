package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.attendance.AttendanceHistoryDto;
import com.erp.system.erpsystem.dto.attendance.TodayAttendanceDto;
import com.erp.system.erpsystem.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // Attendance history (requesterId needed to check hierarchy)
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getAttendanceHistory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer userId)
    {
        try {
            String token = authHeader.substring(7);
            List<AttendanceHistoryDto> history = attendanceService.getAttendanceHistory(userId, token);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error fetching attendance history: " + e.getMessage());
        }
    }

    // Today's attendance
    @GetMapping("/today/{userId}")
    public ResponseEntity<?> getTodayAttendance(@PathVariable Integer userId) {
        try {
            TodayAttendanceDto today = attendanceService.getTodayAttendance(userId);
            return ResponseEntity.ok(today);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error fetching today's attendance: " + e.getMessage());
        }
    }

    // Check-in
    @PostMapping("/checkin/{userId}")
    public ResponseEntity<?> checkIn(@PathVariable Integer userId) {
        try {
            TodayAttendanceDto response = attendanceService.checkIn(userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Check-in failed: " + e.getMessage());
        }
    }

    // Check-out
    @PostMapping("/checkout/{userId}")
    public ResponseEntity<?> checkOut(@PathVariable Integer userId) {
        try {
            TodayAttendanceDto response = attendanceService.checkOut(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Check-out failed: " + e.getMessage());
        }
    }
}
