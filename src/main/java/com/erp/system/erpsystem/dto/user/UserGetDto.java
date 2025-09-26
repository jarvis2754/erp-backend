package com.erp.system.erpsystem.dto.user;

import com.erp.system.erpsystem.dto.details.UserDetailsDto;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGetDto {
    private String userName;
    private String email;
    private String phoneNumber;
    private Department department;
    private Position position;
    private UserDetailsDto reportingManager;
}
