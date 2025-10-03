package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.dashboard.AttendanceTrendDashboardDto;
import com.erp.system.erpsystem.dto.dashboard.LeaveStatsDashboardDto;
import com.erp.system.erpsystem.dto.dashboard.OrderStatsDashboardDto;
import com.erp.system.erpsystem.model.LeaveRequest;
import com.erp.system.erpsystem.model.Organization;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.*;
import com.erp.system.erpsystem.repository.*;
import com.erp.system.erpsystem.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    // ✅ Attendance Trend
    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PurchaseRequisitionRepository purchaseRequisitionRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    // ✅ Get attendance count for the last 7 days
    public List<AttendanceTrendDashboardDto> getAttendanceTrend(String token) {
        List<AttendanceTrendDashboardDto> trends = new ArrayList<>();
        Integer orgId =jwtUtil.extractOrgId(token);
        Organization organization = organizationRepository
                .findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        // Loop for last 7 days
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            int attendanceCount = attendanceRepository.countByDateAndStatusAndUserOrganization(date, AttendanceStatus.PRESENT, organization);
            long totalEmployee = userRepository.countByPositionInAndOrganizationOrgIdAndJoiningDateLessThanEqual(
                    List.of(Position.INTERN, Position.ASSOCIATE, Position.EXECUTIVE, Position.SENIOR_EXECUTIVE,
                            Position.LEAD, Position.MANAGER, Position.SENIOR_MANAGER),
                    orgId,
                    date
            );


            AttendanceTrendDashboardDto dto = new AttendanceTrendDashboardDto();
            dto.setDate(date.atStartOfDay()); // set as LocalDateTime
            dto.setAttendanceCount(attendanceCount);
            dto.setTotalEmployee(totalEmployee);

            trends.add(dto);
        }

        return trends;
    }

    // ✅ Leave Stats
    public LeaveStatsDashboardDto getLeaveStats(String token) {
        Integer userId = jwtUtil.extractUserId(token); // Extract userId from token
        LeaveStatsDashboardDto statsDto = new LeaveStatsDashboardDto();

        LocalDate janFirst = LocalDate.of(LocalDate.now().getYear(), 1, 1);

        // Fetch all approved leaves for this user starting from Jan 1
        List<LeaveRequest> leaves = leaveRequestRepository
                .findByRequestedByUserIdAndStatusAndStartDateAfter(userId, LeaveStatus.APPROVED, janFirst.minusDays(1));

        for (LeaveRequest leave : leaves) {
            // Only count leaves starting from Jan 1
            if (!leave.getStartDate().isBefore(janFirst)) {
                switch (leave.getLeaveType()) {
                    case PRIVILEGE -> statsDto.setMedical(statsDto.getMedical() + 1);
                    case CASUAL -> statsDto.setCasual(statsDto.getCasual() + 1);
                    case MEDICAL -> statsDto.setPrivilege(statsDto.getPrivilege() + 1);

                }
            }
        }

        return statsDto;
    }

    public OrderStatsDashboardDto getOrderStats(String token) {
        User user = userRepository
                .findById(jwtUtil.extractUserId(token))
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<AttendanceTrendDashboardDto> trends = new ArrayList<>();
        Integer orgId = jwtUtil.extractOrgId(token);
        Organization organization = organizationRepository
                .findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        int count = attendanceRepository.countByDateAndStatusAndUserOrganization(LocalDate.now(), AttendanceStatus.PRESENT, organization);
        OrderStatsDashboardDto os = new OrderStatsDashboardDto();
        os.setTotalEmployee(userRepository.countByOrganizationOrgId(orgId));
        os.setTotalPresent(count);
        long totalEmployee = userRepository.countByPositionInAndOrganizationOrgIdAndJoiningDateLessThanEqual(
                List.of(Position.INTERN, Position.ASSOCIATE, Position.EXECUTIVE, Position.SENIOR_EXECUTIVE,
                        Position.LEAD, Position.MANAGER, Position.SENIOR_MANAGER),
                orgId,
                LocalDate.now()
        );

        os.setTotalAttendanceEligibleEmployee(totalEmployee);
        if (user.getDepartment() == Department.HR || user.getDepartment() == Department.PROCUREMENT || user.getDepartment()==Department.FINANCE) {
            if (user.getDepartment() == Department.HR) {
                os.setPendingApprovals(leaveRequestRepository.countByOrganizationOrgIdAndStatus(orgId, LeaveStatus.PENDING));
            }else{
                os.setPendingApprovals(purchaseRequisitionRepository.countByOrgOrgIdAndStatus(orgId, PRStatus.PENDING));
            }
        }

        if (user.getDepartment() == Department.PROCUREMENT || user.getDepartment() == Department.FINANCE) {
            os.setActivePOs(purchaseOrderRepository.countByOrgOrgIdAndStatus(orgId,POStatus.OPEN));
        }

        return os;
    }

    private boolean isManagerOrAbove(Position position) {
        return switch (position) {
            case MANAGER, SENIOR_MANAGER, DIRECTOR, VICE_PRESIDENT, PRESIDENT, CXO, CEO -> true;
            default -> false;
        };
    }

}
