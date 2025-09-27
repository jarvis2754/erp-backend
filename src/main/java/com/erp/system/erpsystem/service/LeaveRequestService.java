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

    public LeaveRequestDto create(CreateLeaveRequestDto dto, String token) {
        dto.setRequestedById(jwtUtil.extractUserId(token));
        dto.setOrgId(jwtUtil.extractOrgId(token));

        // Fetch the user
        User requester = userRepository.findById(dto.getRequestedById())
                .orElseThrow(() -> new RuntimeException("RequestedBy user not found"));

        // Restrict leave creation for high-level positions
        switch (requester.getPosition()) {
            case CEO:
            case CXO:
            case VICE_PRESIDENT:
            case PRESIDENT:
                throw new RuntimeException("Users with position " + requester.getPosition() + " cannot request leave");
            default:
                break;
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new RuntimeException("endDate must be after or equal to startDate");
        }

        LeaveRequest entity = mapper.createDtoToEntity(dto);
        entity.setStatus(LeaveStatus.PENDING);
        entity.setRequestedBy(requester);

        Organization org = organizationRepository.findById(dto.getOrgId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        entity.setOrganization(org);

        LeaveRequest saved = leaveRequestRepository.save(entity);
        return mapper.entityToDto(saved);
    }


    public LeaveRequestDto getById(Integer id) {
        LeaveRequest lr = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id: " + id));
        return mapper.entityToDto(lr);
    }

    public Page<LeaveRequestDto> listByOrganization(Integer orgId, Pageable pageable, String status) {
        Page<LeaveRequest> page;

        if (status == null || status.isEmpty()) {
            page = leaveRequestRepository.findByOrganization_OrgId(orgId, pageable);
        } else {
            LeaveStatus leaveStatus;
            try {
                leaveStatus = LeaveStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid leave status: " + status);
            }
            page = leaveRequestRepository.findByOrganization_OrgIdAndStatus(orgId, leaveStatus, pageable);
        }

        return page.map(mapper::entityToDto);
    }

    public List<LeaveRequestDto> listByUser(Integer userId) {
        return leaveRequestRepository.findByRequestedBy_UserId(userId)
                .stream().map(mapper::entityToDto).collect(Collectors.toList());
    }

    public LeaveRequestDto update(Integer id, CreateLeaveRequestDto dto) {
        LeaveRequest existing = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id: " + id));

        if (dto.getStartDate() != null) existing.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) existing.setEndDate(dto.getEndDate());
        if (dto.getReason() != null) existing.setReason(dto.getReason());
        if (dto.getLeaveType() != null) existing.setLeaveType(dto.getLeaveType());

        LeaveRequest saved = leaveRequestRepository.save(existing);
        return mapper.entityToDto(saved);
    }

    public LeaveRequestDto changeStatus(String token, Integer leaveId, UpdateLeaveStatusDto dto) {
        Integer approverId = jwtUtil.extractUserId(token);
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id: " + leaveId));

        validateApprover(approver, leave);

        LeaveStatus status;
        try {
            status = LeaveStatus.valueOf(dto.getStatus().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status value: " + dto.getStatus());
        }

        leave.setStatus(status);
        leave.setApprovedBy(approver);

        LeaveRequest saved = leaveRequestRepository.save(leave);
        return mapper.entityToDto(saved);
    }

    private void validateApprover(User approver, LeaveRequest leave) {
        User requestedBy = leave.getRequestedBy();

        // HR can approve any leave
        if (approver.getDepartment() == Department.HR) return;

        // Managers can approve team members of same department
        if (approver.getPosition().ordinal() >= Position.MANAGER.ordinal()) {
            if (requestedBy.getDepartment() == approver.getDepartment()
                    && requestedBy.getPosition().ordinal() < approver.getPosition().ordinal()) {
                return;
            }
        }

        throw new RuntimeException("User " + approver.getUserName() + " is not authorized to approve this leave.");
    }

    public List<LeaveRequestDto> pendingApprovals(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<LeaveRequest> pending;

        if (user.getDepartment() == Department.HR) {
            pending = leaveRequestRepository.findByStatus(LeaveStatus.PENDING);
        } else if (user.getPosition().ordinal() >= Position.MANAGER.ordinal()) {
            pending = leaveRequestRepository.findByRequestedBy_DepartmentAndStatus(user.getDepartment(), LeaveStatus.PENDING);
        } else {
            pending = List.of();
        }

        return pending.stream().map(mapper::entityToDto).collect(Collectors.toList());
    }

    public void delete(Integer id) {
        LeaveRequest existing = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id: " + id));
        leaveRequestRepository.delete(existing);
    }
}
