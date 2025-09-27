package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.attendance.AttendanceHistoryDto;
import com.erp.system.erpsystem.dto.attendance.TodayAttendanceDto;
import com.erp.system.erpsystem.model.Attendance;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.AttendanceStatus;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.repository.AttendanceRepository;
import com.erp.system.erpsystem.repository.UserRepository;
import com.erp.system.erpsystem.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Attendance history
    public List<AttendanceHistoryDto> getAttendanceHistory(Integer userId, String token) {
        User requester = userRepository.findById(jwtUtil.extractUserId(token))
                .orElseThrow(() -> new RuntimeException("User not found"));
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Employee can view own record
        if (requester.getUserId().equals(userId)) return fetchAttendance(targetUser);

        // Lead can view only direct reports
        if (requester.getPosition() == Position.LEAD &&
                !targetUser.getReportingManager().equals(requester)) {
            throw new RuntimeException("Leads can view only their team members");
        }

        // Manager can view department members
        if (requester.getPosition() == Position.MANAGER &&
                !requester.getDepartment().equals(targetUser.getDepartment())) {
            throw new RuntimeException("Managers can view only their department");
        }

        // Director+ can view all
        if (requester.getPosition().ordinal() >= Position.DIRECTOR.ordinal()) {
            return fetchAttendance(targetUser);
        }

        throw new RuntimeException("You are not authorized to view this attendance");
    }

    private List<AttendanceHistoryDto> fetchAttendance(User user) {
        List<Attendance> records = attendanceRepository.findByUser_UserIdOrderByDateDesc(user.getUserId());
        return records.stream().map(a -> {
            double workHours = 0;
            if (a.getCheckIn() != null && a.getCheckOut() != null) {
                Duration duration = Duration.between(a.getCheckIn(), a.getCheckOut());
                workHours = duration.toMinutes() / 60.0;
            }
            return new AttendanceHistoryDto(
                    a.getDate(),
                    a.getCheckIn() != null ? a.getCheckIn().toLocalTime() : null,
                    a.getCheckOut() != null ? a.getCheckOut().toLocalTime() : null,
                    workHours,
                    a.getStatus()
            );
        }).toList();
    }

    // Today's attendance
    public TodayAttendanceDto getTodayAttendance(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByUser_UserIdAndDate(userId, today).orElse(null);

        if (attendance == null) {
            return new TodayAttendanceDto("Not Checked In", null, null);
        } else if (attendance.getCheckIn() != null && attendance.getCheckOut() == null) {
            return new TodayAttendanceDto("Checked In", attendance.getCheckIn().toLocalTime(), null);
        } else {
            return new TodayAttendanceDto("Completed", attendance.getCheckIn().toLocalTime(), attendance.getCheckOut().toLocalTime());
        }
    }

    // Check-in
    public TodayAttendanceDto checkIn(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only operational staff can check-in
        switch (user.getPosition()) {
            case INTERN, ASSOCIATE, EXECUTIVE, SENIOR_EXECUTIVE, LEAD -> { /* allowed */ }
            default -> throw new RuntimeException("Your role cannot mark attendance");
        }

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByUser_UserIdAndDate(userId, today).orElse(null);

        if (attendance == null) {
            attendance = new Attendance();
            attendance.setUser(user);
            attendance.setDate(today);
            attendance.setCheckIn(java.time.LocalDateTime.now());
            attendance.setStatus(AttendanceStatus.PRESENT);
            attendanceRepository.save(attendance);
            return new TodayAttendanceDto("Checked In", attendance.getCheckIn().toLocalTime(), null);
        } else {
            throw new RuntimeException("Already checked in today");
        }
    }

    // Check-out
    public TodayAttendanceDto checkOut(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only operational staff can check-out
        switch (user.getPosition()) {
            case INTERN, ASSOCIATE, EXECUTIVE, SENIOR_EXECUTIVE, LEAD -> { /* allowed */ }
            default -> throw new RuntimeException("Your role cannot mark attendance");
        }

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByUser_UserIdAndDate(userId, today)
                .orElseThrow(() -> new RuntimeException("Not checked in today"));

        if (attendance.getCheckOut() == null) {
            attendance.setCheckOut(java.time.LocalDateTime.now());
            attendanceRepository.save(attendance);
            return new TodayAttendanceDto("Completed", attendance.getCheckIn().toLocalTime(), attendance.getCheckOut().toLocalTime());
        } else {
            throw new RuntimeException("Already checked out today");
        }
    }
}
