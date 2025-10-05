package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.reports.AttendanceOverviewDto;
import com.erp.system.erpsystem.dto.reports.LeavePermissionSummaryDto;
import com.erp.system.erpsystem.dto.reports.RecentRequestDto;
import com.erp.system.erpsystem.model.Attendance;
import com.erp.system.erpsystem.model.LeaveRequest;
import com.erp.system.erpsystem.model.PermissionRequest;
import com.erp.system.erpsystem.model.enums.AttendanceStatus;
import com.erp.system.erpsystem.model.enums.LeaveStatus;
import com.erp.system.erpsystem.model.enums.PermissionStatus;
import com.erp.system.erpsystem.repository.AttendanceRepository;
import com.erp.system.erpsystem.repository.LeaveRequestRepository;
import com.erp.system.erpsystem.repository.PermissionRequestRepository;
import com.erp.system.erpsystem.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AttendanceRepository attendanceRepo;
    private final LeaveRequestRepository leaveRepo;
    private final PermissionRequestRepository permissionRepo;
    private final JwtUtil jwtUtil;

    /**
     * Attendance overview for employee dashboard
     */
    public AttendanceOverviewDto getAttendanceOverview(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);

        // ✅ FIXED: match your entity's relationship (user → userId)
        List<Attendance> records = attendanceRepo.findByUser_UserIdAndDateBetween(userId, startOfMonth, now);

        long present = records.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long absent = records.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        long late = records.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();

        double avgHours = records.stream()
                .filter(a -> a.getWorkHours() != null)
                .mapToDouble(Attendance::getWorkHours)
                .average().orElse(0.0);

        Map<LocalDate, String> attendanceByDay = records.stream()
                .collect(Collectors.toMap(Attendance::getDate, a -> a.getStatus().name(),
                        (oldVal, newVal) -> newVal, LinkedHashMap::new));

        Map<LocalDate, Double> workHoursTrend = records.stream()
                .filter(a -> a.getWorkHours() != null)
                .collect(Collectors.toMap(Attendance::getDate, Attendance::getWorkHours,
                        (oldVal, newVal) -> newVal, LinkedHashMap::new));

        return new AttendanceOverviewDto(
                present,
                absent,
                late,
                avgHours,
                attendanceByDay,
                workHoursTrend
        );
    }

    /**
     * Leave & Permission summary for employee dashboard
     */
    public LeavePermissionSummaryDto getLeavePermissionSummary(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        List<LeaveRequest> leaves = leaveRepo.findByRequestedBy_UserId(userId);
        List<PermissionRequest> permissions = permissionRepo.findByRequestedBy_UserId(userId);

        long totalApprovedLeaves = leaves.stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .count();

        long pendingPermissions = permissions.stream()
                .filter(p -> p.getStatus() == PermissionStatus.PENDING)
                .count();

        // Example leave credit policy: 24 per year
        double remainingBalance = 24 - totalApprovedLeaves;

        // Find next approved upcoming leave
        String nextLeaveRange = leaves.stream()
                .filter(l -> l.getStartDate().isAfter(LocalDate.now()))
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .min(Comparator.comparing(LeaveRequest::getStartDate))
                .map(l -> String.format("%s - %s", l.getStartDate(), l.getEndDate()))
                .orElse("No upcoming leaves");

        // Breakdown by leave type
        Map<String, Long> leaveTypeCount = leaves.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getLeaveType().name(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        // Breakdown by leave status
        Map<String, Long> leaveStatusCount = leaves.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getStatus().name(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        // ✅ Combine latest 5 (3 leaves + 2 permissions)
        List<RecentRequestDto> recent = new ArrayList<>();

        leaves.stream()
                .sorted(Comparator.comparing(LeaveRequest::getCreatedAt).reversed())
                .limit(3)
                .forEach(l -> {
                    RecentRequestDto r = new RecentRequestDto();
                    r.setType("Leave");
                    r.setStatus(l.getStatus().name());
                    r.setDescription(l.getReason());
                    r.setStartDate(l.getStartDate());
                    r.setEndDate(l.getEndDate());
                    recent.add(r);
                });

        permissions.stream()
                .sorted(Comparator.comparing(PermissionRequest::getCreatedAt).reversed())
                .limit(2)
                .forEach(p -> {
                    RecentRequestDto r = new RecentRequestDto();
                    r.setType("Permission");
                    r.setStatus(p.getStatus().name());
                    r.setDescription("Permission from " +
                            p.getStartTime().toLocalTime() + " to " + p.getEndTime().toLocalTime());
                    r.setStartDate(p.getStartTime().toLocalDate());
                    r.setEndDate(p.getEndTime().toLocalDate());
                    recent.add(r);
                });

        LeavePermissionSummaryDto dto = new LeavePermissionSummaryDto();
        dto.setTotalLeavesTaken(totalApprovedLeaves);
        dto.setPendingPermissions(pendingPermissions);
        dto.setRemainingLeaveBalance(remainingBalance);
        dto.setNextLeaveRange(nextLeaveRange);
        dto.setLeaveTypeCount(leaveTypeCount);
        dto.setLeaveStatusCount(leaveStatusCount);
        dto.setRecentRequests(recent);

        return dto;
    }
}
