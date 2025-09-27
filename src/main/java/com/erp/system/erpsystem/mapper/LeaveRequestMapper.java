package com.erp.system.erpsystem.mapper;

import com.erp.system.erpsystem.dto.leave.CreateLeaveRequestDto;
import com.erp.system.erpsystem.dto.leave.LeaveRequestDto;
import com.erp.system.erpsystem.model.LeaveRequest;
import org.springframework.stereotype.Component;

@Component
public class LeaveRequestMapper {

    public LeaveRequestDto entityToDto(LeaveRequest entity) {
        if (entity == null) return null;

        LeaveRequestDto dto = LeaveRequestDto.builder()
                .leaveId(entity.getLeaveId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .reason(entity.getReason())
                .status(entity.getStatus() == null ? null : entity.getStatus().name())
                .leaveType(entity.getLeaveType() == null ? null : entity.getLeaveType().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();

        if (entity.getRequestedBy() != null) {
            dto.setRequestedById(entity.getRequestedBy().getUserId());
            dto.setRequestedByName(entity.getRequestedBy().getUserName());
        }

        if (entity.getApprovedBy() != null) {
            dto.setApprovedById(entity.getApprovedBy().getUserId());
            dto.setApprovedByName(entity.getApprovedBy().getUserName());
        }

        if (entity.getOrganization() != null) {
            dto.setOrgId(entity.getOrganization().getOrgId());
            dto.setOrgName(entity.getOrganization().getOrgName());
        }

        return dto;
    }

    public LeaveRequest createDtoToEntity(CreateLeaveRequestDto dto) {
        if (dto == null) return null;

        LeaveRequest entity = LeaveRequest.builder()
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .leaveType(dto.getLeaveType())
                .status(null) // default PENDING
                .build();

        return entity;
    }
}

