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

    // Create a permission request (auth token required)
    public PermissionRequestDto create(CreatePermissionRequestDto dto, String token) {
        // set requester & org from token
        dto.setRequestedById(jwtUtil.extractUserId(token));
        dto.setOrgId(jwtUtil.extractOrgId(token));

        User requester = userRepository.findById(dto.getRequestedById())
                .orElseThrow(() -> new RuntimeException("RequestedBy user not found"));

        // block very high-level execs from creating permission requests
        switch (requester.getPosition()) {
            case CEO:
            case CXO:
            case PRESIDENT:
            case VICE_PRESIDENT:
                throw new RuntimeException("Users with position " + requester.getPosition() + " cannot request permissions");
            default:
                break;
        }

        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new RuntimeException("endTime must be after startTime");
        }

        PermissionRequest entity = mapper.createDtoToEntity(dto);
        entity.setStatus(PermissionStatus.PENDING);
        entity.setRequestedBy(requester);


        Organization org = organizationRepository.findById(dto.getOrgId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        entity.setOrganization(org);

        PermissionRequest saved = permissionRepository.save(entity);
        return mapper.entityToDto(saved);
    }

    // Get by id
    public PermissionRequestDto getById(Integer id) {
        PermissionRequest p = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PermissionRequest not found with id: " + id));
        return mapper.entityToDto(p);
    }

    // List by organization with optional status filter (paged)
    public Page<PermissionRequestDto> listByOrganization(Integer orgId, Pageable pageable, String status) {
        Page<PermissionRequest> page;
        if (status == null || status.isEmpty()) {
            page = permissionRepository.findByOrganization_OrgId(orgId, pageable);
        } else {
            PermissionStatus s;
            try { s = PermissionStatus.valueOf(status.toUpperCase()); }
            catch (IllegalArgumentException e) { throw new RuntimeException("Invalid permission status: " + status); }
            page = permissionRepository.findByOrganization_OrgIdAndStatus(orgId, s, pageable);
        }
        return page.map(mapper::entityToDto);
    }

    // List by user
    public List<PermissionRequestDto> listByUser(Integer userId) {
        return permissionRepository.findByRequestedBy_UserId(userId)
                .stream().map(mapper::entityToDto).collect(Collectors.toList());
    }

    // Update (only requester may update while PENDING)
    public PermissionRequestDto update(String token, Integer id, CreatePermissionRequestDto dto) {
        Integer userId = jwtUtil.extractUserId(token);

        // Fetch the managed entity
        PermissionRequest existing = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PermissionRequest not found with id: " + id));

        // Authorization check
        if (!existing.getRequestedBy().getUserId().equals(userId)) {
            throw new RuntimeException("Only requester can update the permission request");
        }

        if (existing.getStatus() != PermissionStatus.PENDING) {
            throw new RuntimeException("Only PENDING requests can be updated");
        }

        if (dto.getStartTime() != null && dto.getEndTime() != null && dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new RuntimeException("endTime must be after startTime");
        }

        // Update fields only if provided
        if (dto.getStartTime() != null) existing.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) existing.setEndTime(dto.getEndTime());
        if (dto.getReason() != null) existing.setReason(dto.getReason());

        // Force updatedAt to current time
        existing.setUpdatedAt(LocalDateTime.now());

        PermissionRequest saved = permissionRepository.save(existing);
        return mapper.entityToDto(saved);
    }


    public PermissionRequestDto changeStatus(String token, Integer permissionId, UpdatePermissionStatusDto dto) {
        Integer approverId = jwtUtil.extractUserId(token);
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        // Fetch the managed entity
        PermissionRequest permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("PermissionRequest not found"));

        // Authorization
        if (!isAuthorizedToApprove(approver, permission.getRequestedBy())) {
            throw new RuntimeException("User not authorized to approve this permission");
        }

        PermissionStatus status;
        try {
            status = PermissionStatus.valueOf(dto.getStatus().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status: " + dto.getStatus());
        }

        permission.setStatus(status);
        permission.setApprovedBy(approver);

        // Force updatedAt to current time
        permission.setUpdatedAt(LocalDateTime.now());

        PermissionRequest saved = permissionRepository.save(permission);
        return mapper.entityToDto(saved);
    }


    private boolean isAuthorizedToApprove(User approver, User requester) {
        // HR can approve any
        if (approver.getDepartment() == Department.HR) return true;
        // Managers can approve their department subordinates
        if (approver.getPosition().ordinal() >= Position.MANAGER.ordinal()
                && approver.getDepartment() == requester.getDepartment()
                && requester.getPosition().ordinal() < approver.getPosition().ordinal()) {
            return true;
        }
        return false;
    }

    // Pending approvals visible to caller
    public List<PermissionRequestDto> pendingApprovals(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        List<PermissionRequest> pending;
        if (user.getDepartment() == Department.HR) {
            pending = permissionRepository.findByStatus(PermissionStatus.PENDING);
        } else if (user.getPosition().ordinal() >= Position.MANAGER.ordinal()) {
            pending = permissionRepository.findByRequestedBy_DepartmentAndStatus(user.getDepartment(), PermissionStatus.PENDING);
        } else {
            pending = List.of(); // regular employees don't see others' pending requests
        }

        return pending.stream().map(mapper::entityToDto).collect(Collectors.toList());
    }

    // Delete (requester or HR)
    public void delete(String token, Integer id) {
        Integer userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        PermissionRequest existing = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PermissionRequest not found with id: " + id));

        boolean isRequester = existing.getRequestedBy().getUserId().equals(userId);
        boolean isHR = user.getDepartment() == Department.HR;

        if (!isRequester && !isHR) throw new RuntimeException("Not authorized to delete this request");

        permissionRepository.delete(existing);
    }
}
