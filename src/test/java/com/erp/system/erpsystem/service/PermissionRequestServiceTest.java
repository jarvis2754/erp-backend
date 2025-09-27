package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.permission.CreatePermissionRequestDto;
import com.erp.system.erpsystem.dto.permission.PermissionRequestDto;
import com.erp.system.erpsystem.dto.permission.UpdatePermissionStatusDto;
import com.erp.system.erpsystem.mapper.PermissionRequestMapper;
import com.erp.system.erpsystem.model.*;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.PermissionStatus;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.repository.OrganizationRepository;
import com.erp.system.erpsystem.repository.PermissionRequestRepository;
import com.erp.system.erpsystem.repository.UserRepository;
import com.erp.system.erpsystem.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionRequestServiceTest {

    @Mock private PermissionRequestRepository permissionRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private PermissionRequestMapper mapper;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private PermissionRequestService service;

    private String token;
    private User employee;
    private User manager;
    private Organization org;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        token = "dummyToken";

        employee = new User();
        employee.setUserId(1);
        employee.setPosition(Position.EXECUTIVE);
        employee.setDepartment(Department.IT);

        manager = new User();
        manager.setUserId(2);
        manager.setPosition(Position.MANAGER);
        manager.setDepartment(Department.IT);

        org = new Organization();
        org.setOrgId(100);
    }

    // ================= CREATE =================
    @Test
    void testCreatePermissionRequest_Success() {
        CreatePermissionRequestDto dto = new CreatePermissionRequestDto();
        dto.setStartTime(LocalDateTime.now());
        dto.setEndTime(LocalDateTime.now().plusHours(2));
        dto.setReason("Meeting");
        dto.setRequestedById(employee.getUserId());
        dto.setOrgId(org.getOrgId());

        PermissionRequest entity = new PermissionRequest();
        entity.setRequestedBy(employee);
        entity.setOrganization(org);
        entity.setStatus(PermissionStatus.PENDING);

        when(jwtUtil.extractUserId(token)).thenReturn(employee.getUserId());
        when(jwtUtil.extractOrgId(token)).thenReturn(org.getOrgId());
        when(userRepository.findById(employee.getUserId())).thenReturn(Optional.of(employee));
        when(organizationRepository.findById(org.getOrgId())).thenReturn(Optional.of(org));
        when(mapper.createDtoToEntity(dto)).thenReturn(entity);
        when(permissionRepository.save(entity)).thenReturn(entity);
        when(mapper.entityToDto(entity)).thenReturn(new PermissionRequestDto());

        PermissionRequestDto result = service.create(dto, token);
        assertNotNull(result);
        verify(permissionRepository).save(entity);
    }

    @Test
    void testCreatePermissionRequest_CEO_Fails() {
        employee.setPosition(Position.CEO);

        CreatePermissionRequestDto dto = new CreatePermissionRequestDto();
        dto.setStartTime(LocalDateTime.now());
        dto.setEndTime(LocalDateTime.now().plusHours(1));

        when(jwtUtil.extractUserId(token)).thenReturn(employee.getUserId());
        when(userRepository.findById(employee.getUserId())).thenReturn(Optional.of(employee));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.create(dto, token));
        assertTrue(ex.getMessage().contains("cannot request permissions"));
    }

    // ================= UPDATE =================
    @Test
    void testUpdatePermissionRequest_Success() {
        PermissionRequest existing = new PermissionRequest();
        existing.setRequestedBy(employee);
        existing.setStatus(PermissionStatus.PENDING);

        CreatePermissionRequestDto dto = new CreatePermissionRequestDto();
        dto.setStartTime(LocalDateTime.now());
        dto.setEndTime(LocalDateTime.now().plusHours(2));
        dto.setReason("Updated reason");

        when(jwtUtil.extractUserId(token)).thenReturn(employee.getUserId());
        when(permissionRepository.findById(1)).thenReturn(Optional.of(existing));
        when(permissionRepository.save(existing)).thenReturn(existing);
        when(mapper.entityToDto(existing)).thenReturn(new PermissionRequestDto());

        PermissionRequestDto result = service.update(token, 1, dto);
        assertNotNull(result);
        assertEquals(existing.getReason(), dto.getReason());
    }

    @Test
    void testUpdatePermissionRequest_NotOwner_Fails() {
        User other = new User();
        other.setUserId(99);
        PermissionRequest existing = new PermissionRequest();
        existing.setRequestedBy(other);
        existing.setStatus(PermissionStatus.PENDING);

        when(jwtUtil.extractUserId(token)).thenReturn(employee.getUserId());
        when(permissionRepository.findById(1)).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.update(token, 1, new CreatePermissionRequestDto()));
        assertTrue(ex.getMessage().contains("Only requester can update"));
    }

    // ================= CHANGE STATUS =================
    @Test
    void testChangeStatus_Success() {
        PermissionRequest request = new PermissionRequest();
        request.setRequestedBy(employee);
        request.setStatus(PermissionStatus.PENDING);

        UpdatePermissionStatusDto dto = new UpdatePermissionStatusDto();
        dto.setStatus("APPROVED");

        when(jwtUtil.extractUserId(token)).thenReturn(manager.getUserId());
        when(userRepository.findById(manager.getUserId())).thenReturn(Optional.of(manager));
        when(permissionRepository.findById(10)).thenReturn(Optional.of(request));
        when(permissionRepository.save(request)).thenReturn(request);
        when(mapper.entityToDto(request)).thenReturn(new PermissionRequestDto());

        PermissionRequestDto result = service.changeStatus(token, 10, dto);
        assertNotNull(result);
        assertEquals(PermissionStatus.APPROVED, request.getStatus());
        assertEquals(manager, request.getApprovedBy());
    }

    @Test
    void testChangeStatus_NotAuthorized_Fails() {
        User other = new User();
        other.setUserId(99);
        other.setDepartment(Department.IT);
        other.setPosition(Position.EXECUTIVE);

        PermissionRequest request = new PermissionRequest();
        request.setRequestedBy(employee);
        request.setStatus(PermissionStatus.PENDING);

        UpdatePermissionStatusDto dto = new UpdatePermissionStatusDto();
        dto.setStatus("APPROVED");

        when(jwtUtil.extractUserId(token)).thenReturn(other.getUserId());
        when(userRepository.findById(other.getUserId())).thenReturn(Optional.of(other));
        when(permissionRepository.findById(10)).thenReturn(Optional.of(request));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.changeStatus(token, 10, dto));
        assertTrue(ex.getMessage().contains("not authorized"));
    }

    // ================= PENDING APPROVALS =================
    @Test
    void testPendingApprovals_HR() {
        User hr = new User();
        hr.setUserId(1000);
        hr.setDepartment(Department.HR);

        PermissionRequest pendingRequest = new PermissionRequest();
        pendingRequest.setStatus(PermissionStatus.PENDING);

        when(jwtUtil.extractUserId(token)).thenReturn(hr.getUserId());
        when(userRepository.findById(hr.getUserId())).thenReturn(Optional.of(hr));
        when(permissionRepository.findByStatus(PermissionStatus.PENDING))
                .thenReturn(List.of(pendingRequest));
        when(mapper.entityToDto(pendingRequest)).thenReturn(new PermissionRequestDto());

        List<PermissionRequestDto> result = service.pendingApprovals(token);
        assertEquals(1, result.size());
    }

    // ================= DELETE =================
    @Test
    void testDelete_ByRequester() {
        PermissionRequest request = new PermissionRequest();
        request.setRequestedBy(employee);

        when(jwtUtil.extractUserId(token)).thenReturn(employee.getUserId());
        when(userRepository.findById(employee.getUserId())).thenReturn(Optional.of(employee));
        when(permissionRepository.findById(1)).thenReturn(Optional.of(request));

        assertDoesNotThrow(() -> service.delete(token, 1));
        verify(permissionRepository).delete(request);
    }

    @Test
    void testDelete_NotAuthorized_Fails() {
        User other = new User();
        other.setUserId(99);
        other.setDepartment(Department.IT);

        PermissionRequest request = new PermissionRequest();
        request.setRequestedBy(employee);

        when(jwtUtil.extractUserId(token)).thenReturn(other.getUserId());
        when(userRepository.findById(other.getUserId())).thenReturn(Optional.of(other));
        when(permissionRepository.findById(1)).thenReturn(Optional.of(request));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.delete(token, 1));
        assertTrue(ex.getMessage().contains("Not authorized"));
    }

    // ================= LIST =================
    @Test
    void testListByUser() {
        PermissionRequest request = new PermissionRequest();
        when(permissionRepository.findByRequestedBy_UserId(employee.getUserId()))
                .thenReturn(List.of(request));
        when(mapper.entityToDto(request)).thenReturn(new PermissionRequestDto());

        List<PermissionRequestDto> result = service.listByUser(employee.getUserId());
        assertEquals(1, result.size());
    }

    @Test
    void testListByOrganization() {
        PermissionRequest request = new PermissionRequest();
        Page<PermissionRequest> page = new PageImpl<>(List.of(request));

        when(permissionRepository.findByOrganization_OrgId(org.getOrgId(), Pageable.unpaged()))
                .thenReturn(page);
        when(mapper.entityToDto(request)).thenReturn(new PermissionRequestDto());

        Page<PermissionRequestDto> result = service.listByOrganization(org.getOrgId(), Pageable.unpaged(), null);
        assertEquals(1, result.getContent().size());
    }
}
