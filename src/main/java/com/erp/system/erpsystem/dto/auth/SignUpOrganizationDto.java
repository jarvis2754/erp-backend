package com.erp.system.erpsystem.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpOrganizationDto {
      private SignUpRequestDTO user;
      private OrganizationSignUpDto organization;
}
