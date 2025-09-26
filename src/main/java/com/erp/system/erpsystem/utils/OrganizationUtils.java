package com.erp.system.erpsystem.utils;

import com.erp.system.erpsystem.dto.auth.SignUpOrganizationDto;
import com.erp.system.erpsystem.model.Organization;

public class OrganizationUtils {
    public static Organization getOrganization(SignUpOrganizationDto userDTO) {
        Organization organization= new Organization();
        organization.setOrgName(userDTO.getOrganization().getOrgName());
        organization.setOrgCode(userDTO.getOrganization().getOrgCode());
        organization.setEmail(userDTO.getOrganization().getEmail());
        organization.setPhoneNumber(userDTO.getOrganization().getPhoneNumber());
        organization.setCountry(userDTO.getOrganization().getCountry());
        organization.setRegisteredAddress(userDTO.getOrganization().getRegisteredAddress());
        return organization;
    }
}
