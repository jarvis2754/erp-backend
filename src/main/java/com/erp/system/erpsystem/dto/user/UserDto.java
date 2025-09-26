package com.erp.system.erpsystem.dto.user;

import com.erp.system.erpsystem.dto.details.UserDetailsDto;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto extends UserGetDto {
    private int userId;
    private String uuId;
    private LocalDate joiningDate;
    private String orgId;

    public UserDto(String userName, String email, String phoneNumber, Department department, Position position, UserDetailsDto reportingManager, int userId, String uuId, LocalDate joiningDate, String orgId) {
        super(userName, email, phoneNumber, department, position, reportingManager);
        this.userId = userId;
        this.uuId = uuId;
        this.joiningDate = joiningDate;
        this.orgId = orgId;
    }
}
