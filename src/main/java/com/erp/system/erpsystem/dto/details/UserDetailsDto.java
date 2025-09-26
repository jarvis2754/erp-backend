package com.erp.system.erpsystem.dto.details;

import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsDto {
    private int userId;
    private String uuId;
    private String userName;
    private String email;
    private Department department;
    private Position position;
}
