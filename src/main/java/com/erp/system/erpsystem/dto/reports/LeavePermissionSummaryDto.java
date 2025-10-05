package com.erp.system.erpsystem.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeavePermissionSummaryDto {
    private long totalLeavesTaken;
    private long pendingPermissions;
    private double remainingLeaveBalance;
    private String nextLeaveRange; // e.g., "10 Oct - 12 Oct"

    private Map<String, Long> leaveTypeCount; // Sick -> 5, Casual -> 3
    private Map<String, Long> leaveStatusCount; // Applied -> 2, Approved -> 5, Rejected -> 1
    private List<RecentRequestDto> recentRequests; // last few leave/permission requests

    // getters and setters
}

