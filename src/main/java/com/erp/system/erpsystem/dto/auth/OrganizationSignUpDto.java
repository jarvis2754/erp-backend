package com.erp.system.erpsystem.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationSignUpDto {
    private String orgCode;
    private String orgName;
    private String phoneNumber;
    private String email;
    private String country;
    private String registeredAddress;
}
