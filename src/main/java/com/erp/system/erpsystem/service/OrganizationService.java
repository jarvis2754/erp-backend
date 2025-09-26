package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.organization.OrganizationRequestDto;
import com.erp.system.erpsystem.dto.organization.OrganizationResponseDto;
import com.erp.system.erpsystem.mapper.OrganizationMapper;
import com.erp.system.erpsystem.model.Organization;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.repository.OrganizationRepository;
import com.erp.system.erpsystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final OrganizationMapper mapper;

    public OrganizationService(OrganizationRepository organizationRepository,
                               UserRepository userRepository,
                               OrganizationMapper mapper) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    // Create Organization
    public OrganizationResponseDto createOrganization(OrganizationRequestDto dto) {
        Organization branchOf = null;
        User owner = null;

        if (dto.getBranchOfId() != null) {
            branchOf = organizationRepository.findById(dto.getBranchOfId())
                    .orElseThrow(() -> new RuntimeException("BranchOf Organization not found"));
        }

        if (dto.getOwnerId() != null) {
            owner = userRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
        }

        Organization org = mapper.toEntity(dto, branchOf, owner);
        Organization saved = organizationRepository.save(org);
        return mapper.toDto(saved);
    }

    // Get Organization by ID
    public OrganizationResponseDto getOrganization(Integer orgId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        return mapper.toDto(org);
    }

    // Get all Organizations
    public List<OrganizationResponseDto> getAllOrganizations() {
        return mapper.toDtoList(organizationRepository.findAll());
    }

    // Get branches of an Organization
    public List<OrganizationResponseDto> getBranches(Integer orgId) {
        Organization parent = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        return mapper.toDtoList(parent.getBranches());
    }

    // Update Organization
    public OrganizationResponseDto updateOrganization(Integer orgId, OrganizationRequestDto dto) {
        Organization existing = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        // Update basic fields only if the DTO values are not null
        if (dto.getOrgCode() != null) existing.setOrgCode(dto.getOrgCode());
        if (dto.getOrgName() != null) existing.setOrgName(dto.getOrgName());
        if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) existing.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getCountry() != null) existing.setCountry(dto.getCountry());
        if (dto.getGstVatNumber() != null) existing.setGstVatNumber(dto.getGstVatNumber());
        if (dto.getPanTinNumber() != null) existing.setPanTinNumber(dto.getPanTinNumber());
        if (dto.getTaxId() != null) existing.setTaxId(dto.getTaxId());
        if (dto.getRegisteredAddress() != null) existing.setRegisteredAddress(dto.getRegisteredAddress());
        if (dto.getCurrency() != null) existing.setCurrency(dto.getCurrency());
        if (dto.getFiscalYear() != null) existing.setFiscalYear(dto.getFiscalYear());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());

        // Update relationships
        if (dto.getBranchOfId() != null) {
            Organization branchOf = organizationRepository.findById(dto.getBranchOfId())
                    .orElseThrow(() -> new RuntimeException("BranchOf Organization not found"));
            existing.setBranchOf(branchOf);
        }

        if (dto.getOwnerId() != null) {
            User owner = userRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            existing.setOwner(owner);
        }

        Organization saved = organizationRepository.save(existing);
        return mapper.toDto(saved);
    }


    // Delete Organization
    public void deleteOrganization(Integer orgId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        organizationRepository.delete(org);
    }

    public Organization registerOrganization(Organization organization) { return organizationRepository.save(organization); }
}
