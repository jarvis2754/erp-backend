package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.leave.CreateLeaveRequestDto;
import com.erp.system.erpsystem.dto.leave.LeaveRequestDto;
import com.erp.system.erpsystem.dto.leave.UpdateLeaveStatusDto;
import com.erp.system.erpsystem.model.LeaveRequest;
import com.erp.system.erpsystem.model.Organization;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.LeaveStatus;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.repository.LeaveRequestRepository;
import com.erp.system.erpsystem.repository.OrganizationRepository;
import com.erp.system.erpsystem.repository.UserRepository;
import com.erp.system.erpsystem.mapper.LeaveRequestMapper;
import com.erp.system.erpsystem.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final LeaveRequestMapper mapper;
    private final JwtUtil jwtUtil;

    @Autowired
    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository,
                               UserRepository userRepository,
                               OrganizationRepository organizationRepository,
                               LeaveRequestMapper mapper,
                               JwtUtil jwtUtil) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.mapper = mapper;
        this.jwtUtil = jwtUtil;
    }

    /** CREATE LEAVE REQUEST **/
    public LeaveRequestDto create(CreateLeaveRequestDto dto, String token) {
        Integer userId = jwtUtil.extractUserId(token);
        Integer orgId = jwtUtil.extractOrgId(token);

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("RequestedBy user not found"));

        // Restrict high-level positions from requesting leave
        if (isHighLevelPosition(requester.getPosition())) {
            throw new RuntimeException("Users with position " + requester.getPosition() + " cannot request leave");
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new RuntimeException("End date must be after or equal to start date");
        }

        LeaveRequest leave = mapper.createDtoToEntity(dto);
        leave.setRequestedBy(requester);
        leave.setStatus(LeaveStatus.PENDING);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        leave.setOrganization(org);

        return mapper.entityToDto(leaveRequestRepository.save(leave));
    }

    /** GET BY ID **/
    public LeaveRequestDto getById(Integer id) {
        return leaveRequestRepository.findById(id)
                .map(mapper::entityToDto)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id: " + id));
    }

    /** LIST BY ORGANIZATION **/
    public Page<LeaveRequestDto> listByOrganization(String orgCode, Pageable pageable, String status) {
        Page<LeaveRequest> page;
        if (status == null || status.isEmpty()) {
            page = leaveRequestRepository.findByOrganization_OrgCodeOrderByCreatedAtDesc(orgCode, pageable);
        } else {
            LeaveStatus leaveStatus = parseLeaveStatus(status);
            page = leaveRequestRepository.findByOrganization_OrgCodeAndStatusOrderByCreatedAtDesc(orgCode, leaveStatus, pageable);
        }
        return page.map(mapper::entityToDto);
    }

    /** LIST BY USER **/
    public List<LeaveRequestDto> listByUser(Integer userId) {
        return leaveRequestRepository.findByRequestedBy_UserId(userId)
                .stream().map(mapper::entityToDto).collect(Collectors.toList());
    }

    /** LIST BY CURRENT USER **/
    public List<LeaveRequestDto> listByCurrentUser(Integer userId) {
        return leaveRequestRepository.findByRequestedBy_UserIdOrderByCreatedAtDesc(userId)
                .stream().map(mapper::entityToDto).collect(Collectors.toList());
    }

    /** UPDATE LEAVE REQUEST **/
    public LeaveRequestDto update(Integer id, CreateLeaveRequestDto dto) {
        LeaveRequest existing = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id: " + id));

        if (dto.getStartDate() != null) existing.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) existing.setEndDate(dto.getEndDate());
        if (dto.getReason() != null) existing.setReason(dto.getReason());
        if (dto.getLeaveType() != null) existing.setLeaveType(dto.getLeaveType());

        existing.setUpdatedAt(LocalDateTime.now());
        return mapper.entityToDto(leaveRequestRepository.save(existing));
    }

    /** CHANGE STATUS **/
    public LeaveRequestDto changeStatus(String token, Integer leaveId, UpdateLeaveStatusDto dto) {
        Integer approverId = jwtUtil.extractUserId(token);
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id: " + leaveId));

        LeaveStatus newStatus = parseLeaveStatus(dto.getStatus());

        // Validate if approver is allowed to approve
        if (!canApprove(approver, leave)) {
            throw new RuntimeException("User " + approver.getUserName() + " is not authorized to approve this leave.");
        }

        leave.setStatus(newStatus);
        leave.setApprovedBy(approver);
        leave.setUpdatedAt(LocalDateTime.now());

        return mapper.entityToDto(leaveRequestRepository.save(leave));
    }

    /** DELETE **/
    public void delete(Integer id) {
        LeaveRequest existing = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id: " + id));
        leaveRequestRepository.delete(existing);
    }

    /** PENDING APPROVALS **/
    public List<LeaveRequestDto> pendingApprovals(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<LeaveRequest> pending;
        if (user.getDepartment() == Department.HR) {
            pending = leaveRequestRepository.findByStatus(LeaveStatus.PENDING);
        } else if (isManagerOrAbove(user.getPosition())) {
            pending = leaveRequestRepository.findByRequestedBy_DepartmentAndStatus(user.getDepartment(), LeaveStatus.PENDING);
        } else {
            pending = List.of();
        }

        return pending.stream().map(mapper::entityToDto).collect(Collectors.toList());
    }

    /** HELPERS **/
    private boolean isHighLevelPosition(Position position) {
        return switch (position) {
            case CEO, CXO, VICE_PRESIDENT, PRESIDENT, DIRECTOR -> true;
            default -> false;
        };
    }

    private boolean isManagerOrAbove(Position position) {
        return switch (position) {
            case MANAGER, SENIOR_MANAGER, DIRECTOR, VICE_PRESIDENT, PRESIDENT, CXO, CEO -> true;
            default -> false;
        };
    }

    private LeaveStatus parseLeaveStatus(String status) {
        try {
            return LeaveStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid leave status: " + status);
        }
    }

    private boolean canApprove(User approver, LeaveRequest leave) {
        User requester = leave.getRequestedBy();
        Position approverPos = approver.getPosition();
        Position requesterPos = requester.getPosition();

        // HR can approve any if position >= MANAGER
        if (approver.getDepartment() == Department.HR && approverPos.ordinal() >= Position.MANAGER.ordinal()) {
            return true;
        }

        // Director can approve anyone
        if (approverPos == Position.DIRECTOR) {
            return true;
        }

        // Managers can approve subordinates in same department
        if (approverPos.ordinal() > requesterPos.ordinal() &&
                approverPos.ordinal() <= Position.DIRECTOR.ordinal() && // exclude CXO, CEO
                approver.getDepartment() == requester.getDepartment()) {
            return true;
        }

        return false;
    }

}
