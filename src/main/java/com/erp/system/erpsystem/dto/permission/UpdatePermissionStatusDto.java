package com.erp.system.erpsystem.dto.permission;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePermissionStatusDto {

    @NotBlank(message = "status is required and should be one of: PENDING, APPROVED, REJECTED, CANCELLED")
    private String status;

    private Integer approvedById;
    private String comment;
}

