package com.erp.system.erpsystem.mapper;

import com.erp.system.erpsystem.dto.permission.CreatePermissionRequestDto;
import com.erp.system.erpsystem.dto.permission.PermissionRequestDto;
import com.erp.system.erpsystem.model.PermissionRequest;
import org.springframework.stereotype.Component;

@Component
public class PermissionRequestMapper {

    public PermissionRequest createDtoToEntity(CreatePermissionRequestDto dto) {
        if (dto == null) return null;
        return PermissionRequest.builder()
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .reason(dto.getReason())
                .build();
    }

    public PermissionRequestDto entityToDto(PermissionRequest e) {
        if (e == null) return null;
        return PermissionRequestDto.builder()
                .permissionId(e.getPermissionId())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .reason(e.getReason())
                .status(e.getStatus() == null ? null : e.getStatus().name())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .requestedById(e.getRequestedBy() != null ? e.getRequestedBy().getUserId() : null)
                .requestedByName(e.getRequestedBy() != null ? e.getRequestedBy().getUserName() : null)
                .approvedById(e.getApprovedBy() != null ? e.getApprovedBy().getUserId() : null)
                .approvedByName(e.getApprovedBy() != null ? e.getApprovedBy().getUserName() : null)
                .orgId(e.getOrganization() != null ? e.getOrganization().getOrgId() : null)
                .orgName(e.getOrganization() != null ? e.getOrganization().getOrgName() : null)
                .build();
    }
}
