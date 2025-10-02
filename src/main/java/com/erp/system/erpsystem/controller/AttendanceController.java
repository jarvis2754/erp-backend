package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.attendance.AttendanceHistoryDto;
import com.erp.system.erpsystem.dto.attendance.TodayAttendanceDto;
import com.erp.system.erpsystem.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) // default 10 records per page
    {
        try {
            String token = authHeader.substring(7);
            Page<AttendanceHistoryDto> history = attendanceService.getAttendanceHistory(userId, token, page, size);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error fetching attendance history: " + e.getMessage());
        }
    }


    // Today's attendance
    @GetMapping("/today")
    public ResponseEntity<?> getTodayAttendance(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            TodayAttendanceDto today = attendanceService.getTodayAttendance(token);
            return ResponseEntity.ok(today);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error fetching today's attendance: " + e.getMessage());
        }
    }

    // Check-in
    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(@RequestHeader("Authorization") String authHeader) {
        try {
            String token =authHeader.substring(7);
            TodayAttendanceDto response = attendanceService.checkIn(token);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Check-in failed: " + e.getMessage());
        }
    }

    // Check-out
    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(@RequestHeader("Authorization") String authHeader) {
        try {
            String token =authHeader.substring(7);
            TodayAttendanceDto response = attendanceService.checkOut(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Check-out failed: " + e.getMessage());
        }
    }
}
