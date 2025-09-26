package com.erp.system.erpsystem.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryDto {
    private Integer userId;
    private String userName;
    private String email;
}

