package com.erp.system.erpsystem.dto.auth;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String orgCode;
    private String email;
    private String password;
}
