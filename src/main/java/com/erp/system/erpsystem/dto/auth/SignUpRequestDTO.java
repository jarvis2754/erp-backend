package com.erp.system.erpsystem.dto.auth;

import com.erp.system.erpsystem.model.enums.Position;
import lombok.Data;

@Data
public class SignUpRequestDTO {
    private String userName;
    private String email;
    private String password;
}
