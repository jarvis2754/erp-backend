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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<AttendanceHistoryDto> getAttendanceHistory(Integer userId, String token, int page, int size) {
        User requester = userRepository.findById(jwtUtil.extractUserId(token))
                .orElseThrow(() -> new RuntimeException("User not found"));
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Authorization logic
        if (requester.getUserId().equals(userId) ||
                (requester.getPosition() == Position.LEAD && targetUser.getReportingManager().equals(requester)) ||
                (requester.getPosition() == Position.MANAGER && requester.getDepartment().equals(targetUser.getDepartment())) ||
                requester.getPosition().ordinal() >= Position.DIRECTOR.ordinal()) {

            Pageable pageable = PageRequest.of(page, size);
            Page<Attendance> attendancePage = attendanceRepository.findByUser_UserIdOrderByDateDesc(userId, pageable);

            // Map Attendance -> AttendanceHistoryDto
            return attendancePage.map(attendance -> new AttendanceHistoryDto(
                    attendance.getDate(),
                    attendance.getCheckIn(),
                    attendance.getCheckOut(),
                    attendance.getWorkHours(),
                    attendance.getStatus()
            ));
        }

        throw new RuntimeException("You are not authorized to view this attendance");
    }


    // Today's attendance
    public TodayAttendanceDto getTodayAttendance(String token) {
        User user = userRepository.findById(jwtUtil.extractUserId(token))
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByUser_UserIdAndDate(jwtUtil.extractUserId(token), today).orElse(null);

        if (attendance == null) {
            return new TodayAttendanceDto("Not Checked In", null, null);
        } else if (attendance.getCheckIn() != null && attendance.getCheckOut() == null) {
            return new TodayAttendanceDto("Checked In", attendance.getCheckIn().toLocalTime(), null);
        } else {
            return new TodayAttendanceDto("Completed", attendance.getCheckIn().toLocalTime(), attendance.getCheckOut().toLocalTime());
        }
    }

    // Check-in
    public TodayAttendanceDto checkIn(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only operational staff can check-in
        switch (user.getPosition()) {
            case INTERN, ASSOCIATE, EXECUTIVE, SENIOR_EXECUTIVE, LEAD,MANAGER,SENIOR_MANAGER -> { /* allowed */ }
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
    public TodayAttendanceDto checkOut(String token) {
        Integer userId= jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only operational staff can check-out
        switch (user.getPosition()) {
            case INTERN, ASSOCIATE, EXECUTIVE, SENIOR_EXECUTIVE, LEAD, MANAGER, SENIOR_MANAGER -> { /* allowed */ }
            default -> throw new RuntimeException("Your role cannot mark attendance");
        }

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByUser_UserIdAndDate(userId, today)
                .orElseThrow(() -> new RuntimeException("Not checked in today"));

        if (attendance.getCheckOut() == null) {
            attendance.setCheckOut(java.time.LocalDateTime.now());

            double workHours = 0;

            if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
                workHours = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes() / 60.0;
            }
            attendance.setWorkHours(workHours);
            attendanceRepository.save(attendance);
            return new TodayAttendanceDto("Completed", attendance.getCheckIn().toLocalTime(), attendance.getCheckOut().toLocalTime());
        } else {
            throw new RuntimeException("Already checked out today");
        }
    }
}
