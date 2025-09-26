package com.erp.system.erpsystem.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationSummaryDto {
    private Integer orgId;
    private String orgCode;
    private String orgName;
}
