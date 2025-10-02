package com.erp.system.erpsystem.dto.organization;

import com.erp.system.erpsystem.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationRequestDto {
    private String orgCode;
    private String orgName;
    private String phoneNumber;
    private String email;
    private String country;
    private String gstVatNumber;
    private String panTinNumber;
    private String taxId;
    private String registeredAddress;
    private String currency;
    private String fiscalYear;
    private Status status;
    private String branchOfId;   // Instead of full Organization object
    private String ownerId;      // Instead of User object
}

