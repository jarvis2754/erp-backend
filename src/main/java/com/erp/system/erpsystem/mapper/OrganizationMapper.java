package com.erp.system.erpsystem.mapper;

import com.erp.system.erpsystem.dto.organization.OrganizationRequestDto;
import com.erp.system.erpsystem.dto.organization.OrganizationResponseDto;
import com.erp.system.erpsystem.dto.organization.OrganizationSummaryDto;
import com.erp.system.erpsystem.dto.organization.UserSummaryDto;
import com.erp.system.erpsystem.model.Organization;
import com.erp.system.erpsystem.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrganizationMapper {


    // === Entity → Response DTO ===
    public OrganizationResponseDto toDto(Organization org) {
        if (org == null) return null;

        OrganizationResponseDto dto = new OrganizationResponseDto();
        dto.setOrgId(org.getOrgId());
        dto.setOrgCode(org.getOrgCode());
        dto.setOrgName(org.getOrgName());
        dto.setEmail(org.getEmail());
        dto.setPhoneNumber(org.getPhoneNumber());
        dto.setCountry(org.getCountry());
        dto.setGstVatNumber(org.getGstVatNumber());
        dto.setPanTinNumber(org.getPanTinNumber());
        dto.setTaxId(org.getTaxId());
        dto.setRegisteredAddress(org.getRegisteredAddress());
        dto.setCurrency(org.getCurrency());
        dto.setFiscalYear(org.getFiscalYear());
        dto.setStatus(org.getStatus());
        dto.setCreatedAt(org.getCreatedAt() != null ? org.getCreatedAt().toString() : null);

        dto.setBranchOf(toSummary(org.getBranchOf()));

        if (org.getBranches() != null) {
            dto.setBranches(
                    org.getBranches().stream()
                            .map(this::toSummary)
                            .collect(Collectors.toList())
            );
        }

        dto.setOwner(toUserSummary(org.getOwner()));

        if (org.getUsers() != null) {
            dto.setUsers(
                    org.getUsers().stream()
                            .map(this::toUserSummary)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public List<OrganizationResponseDto> toDtoList(List<Organization> orgs) {
        if (orgs == null) return null;
        return orgs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // === Request DTO → Entity ===
    public Organization toEntity(OrganizationRequestDto dto,Organization branchOf,User owner) {
        if (dto == null) return null;

        Organization org = new Organization();
        org.setOrgCode(dto.getOrgCode());
        org.setOrgName(dto.getOrgName());
        org.setEmail(dto.getEmail());
        org.setPhoneNumber(dto.getPhoneNumber());
        org.setCountry(dto.getCountry());
        org.setGstVatNumber(dto.getGstVatNumber());
        org.setPanTinNumber(dto.getPanTinNumber());
        org.setTaxId(dto.getTaxId());
        org.setRegisteredAddress(dto.getRegisteredAddress());
        org.setCurrency(dto.getCurrency());
        org.setFiscalYear(dto.getFiscalYear());
        org.setStatus(dto.getStatus());

        org.setBranchOf(branchOf);
        org.setOwner(owner);

        return org;
    }

    // === Summaries ===
    public OrganizationSummaryDto toSummary(Organization org) {
        if (org == null) return null;
        OrganizationSummaryDto dto = new OrganizationSummaryDto();
        dto.setOrgId(org.getOrgId());
        dto.setOrgCode(org.getOrgCode());
        dto.setOrgName(org.getOrgName());
        return dto;
    }

    public UserSummaryDto toUserSummary(User user) {
        if (user == null) return null;
        UserSummaryDto dto = new UserSummaryDto();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}

