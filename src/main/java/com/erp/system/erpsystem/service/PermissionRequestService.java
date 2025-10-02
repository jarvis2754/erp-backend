package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.permission.CreatePermissionRequestDto;
import com.erp.system.erpsystem.dto.permission.PermissionRequestDto;
import com.erp.system.erpsystem.dto.permission.UpdatePermissionStatusDto;
import com.erp.system.erpsystem.mapper.PermissionRequestMapper;
import com.erp.system.erpsystem.model.PermissionRequest;
import com.erp.system.erpsystem.model.Organization;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.PermissionStatus;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.repository.PermissionRequestRepository;
import com.erp.system.erpsystem.repository.OrganizationRepository;
import com.erp.system.erpsystem.repository.UserRepository;
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
public class PermissionRequestService {

    private final PermissionRequestRepository permissionRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PermissionRequestMapper mapper;
    private final JwtUtil jwtUtil;

    @Autowired
    public PermissionRequestService(PermissionRequestRepository permissionRepository,
                                    UserRepository userRepository,
                                    OrganizationRepository organizationRepository,
                                    PermissionRequestMapper mapper,
                                    JwtUtil jwtUtil) {
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.mapper = mapper;
        this.jwtUtil = jwtUtil;
    }

    /** CREATE PERMISSION REQUEST **/
    public PermissionRequestDto create(CreatePermissionRequestDto dto, String token) {
        Integer userId = jwtUtil.extractUserId(token);
        Integer orgId = jwtUtil.extractOrgId(token);

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("RequestedBy user not found"));

        if (isHighLevelPosition(requester.getPosition())) {
            throw new RuntimeException("Users with position " + requester.getPosition() + " cannot request permissions");
        }

        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        PermissionRequest permission = mapper.createDtoToEntity(dto);
        permission.setRequestedBy(requester);
        permission.setStatus(PermissionStatus.PENDING);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        permission.setOrganization(org);

        return mapper.entityToDto(permissionRepository.save(permission));
    }

    /** GET BY ID **/
    public PermissionRequestDto getById(Integer id) {
        return permissionRepository.findById(id)
                .map(mapper::entityToDto)
                .orElseThrow(() -> new RuntimeException("PermissionRequest not found with id: " + id));
    }

    /** LIST BY ORGANIZATION **/
    public Page<PermissionRequestDto> listByOrganization(String orgCode, Pageable pageable, String status) {
        Page<PermissionRequest> page;
        if (status == null || status.isEmpty()) {
            page = permissionRepository.findByOrganization_OrgCodeOrderByCreatedAtDesc(orgCode, pageable);
        } else {
            PermissionStatus s = parseStatus(status);
            page = permissionRepository.findByOrganization_OrgCodeAndStatusOrderByCreatedAtDesc(orgCode, s, pageable);
        }
        return page.map(mapper::entityToDto);
    }

    /** LIST BY USER **/
    public List<PermissionRequestDto> listByUser(Integer userId) {
        return permissionRepository.findByRequestedBy_UserId(userId)
                .stream().map(mapper::entityToDto).collect(Collectors.toList());
    }

    /** LIST BY CURRENT USER **/
    public List<PermissionRequestDto> listByCurrentUser(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        return permissionRepository.findByRequestedBy_UserIdOrderByCreatedAtDesc(userId)
                .stream().map(mapper::entityToDto).collect(Collectors.toList());
    }

    /** UPDATE (only PENDING & requester) **/
    public PermissionRequestDto update(String token, Integer id, CreatePermissionRequestDto dto) {
        Integer userId = jwtUtil.extractUserId(token);

        PermissionRequest existing = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PermissionRequest not found"));

        if (!existing.getRequestedBy().getUserId().equals(userId)) {
            throw new RuntimeException("Only requester can update the permission request");
        }

        if (existing.getStatus() != PermissionStatus.PENDING) {
            throw new RuntimeException("Only PENDING requests can be updated");
        }

        if (dto.getStartTime() != null && dto.getEndTime() != null && dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        if (dto.getStartTime() != null) existing.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) existing.setEndTime(dto.getEndTime());
        if (dto.getReason() != null) existing.setReason(dto.getReason());

        existing.setUpdatedAt(LocalDateTime.now());
        return mapper.entityToDto(permissionRepository.save(existing));
    }

    /** CHANGE STATUS **/
    public PermissionRequestDto changeStatus(String token, Integer permissionId, UpdatePermissionStatusDto dto) {
        Integer approverId = jwtUtil.extractUserId(token);
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        PermissionRequest permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("PermissionRequest not found"));

        PermissionStatus newStatus = parseStatus(dto.getStatus());

        if (!canApprove(approver, permission.getRequestedBy())) {
            throw new RuntimeException("User " + approver.getUserName() + " is not authorized to approve this permission");
        }

        permission.setStatus(newStatus);
        permission.setApprovedBy(approver);
        permission.setUpdatedAt(LocalDateTime.now());

        return mapper.entityToDto(permissionRepository.save(permission));
    }

    /** PENDING APPROVALS **/
    public List<PermissionRequestDto> pendingApprovals(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        List<PermissionRequest> pending;
        if (user.getDepartment() == Department.HR) {
            pending = permissionRepository.findByStatus(PermissionStatus.PENDING);
        } else if (isManagerOrAbove(user.getPosition())) {
            pending = permissionRepository.findByRequestedBy_DepartmentAndStatus(user.getDepartment(), PermissionStatus.PENDING);
        } else {
            pending = List.of();
        }

        return pending.stream().map(mapper::entityToDto).collect(Collectors.toList());
    }

    /** DELETE **/
    public void delete(String token, Integer id) {
        Integer userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        PermissionRequest existing = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PermissionRequest not found"));

        boolean isRequester = existing.getRequestedBy().getUserId().equals(userId);
        boolean isHR = user.getDepartment() == Department.HR;

        if (!isRequester && !isHR) throw new RuntimeException("Not authorized to delete this request");

        permissionRepository.delete(existing);
    }

    /** HELPERS **/
    private boolean isHighLevelPosition(Position position) {
        return switch (position) {
            case CEO, CXO, PRESIDENT, VICE_PRESIDENT -> true;
            default -> false;
        };
    }

    private boolean isManagerOrAbove(Position position) {
        return switch (position) {
            case MANAGER, SENIOR_MANAGER, DIRECTOR, VICE_PRESIDENT, PRESIDENT, CXO, CEO -> true;
            default -> false;
        };
    }

    private PermissionStatus parseStatus(String status) {
        try {
            return PermissionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid permission status: " + status);
        }
    }

    private boolean canApprove(User approver, User requester) {
        Position approverPos = approver.getPosition();
        Position requesterPos = requester.getPosition();

        // HR can approve anyone only if position >= MANAGER
        if (approver.getDepartment() == Department.HR && approverPos.ordinal() >= Position.MANAGER.ordinal()) {
            return true;
        }

        // Director can approve anyone
        if (approverPos == Position.DIRECTOR) {
            return true;
        }

        // Managers or Senior Managers can approve subordinates in the same department
        if (isManagerOrAbove(approverPos)
                && approver.getDepartment() == requester.getDepartment()
                && requesterPos.ordinal() < approverPos.ordinal()
                && approverPos.ordinal() <= Position.DIRECTOR.ordinal()) { // exclude CXO, CEO
            return true;
        }

        return false;
    }

}
