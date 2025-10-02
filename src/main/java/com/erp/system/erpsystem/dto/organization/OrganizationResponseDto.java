package com.erp.system.erpsystem.dto.organization;


import com.erp.system.erpsystem.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponseDto {
    private Integer orgId;
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
    private String createdAt;

    private OrganizationSummaryDto branchOf;           // Parent org
    private List<OrganizationSummaryDto> branches;     // Sub-branches
    private UserSummaryDto owner;                      // Owner

}
