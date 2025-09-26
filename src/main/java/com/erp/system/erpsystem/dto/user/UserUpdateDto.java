package com.erp.system.erpsystem.dto.user;

import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {
    private String userName;
    private String email;
    private String phoneNumber;
    private Department department;
    private Position position;
    private String password;
    private String reportingManager;
}
