package com.erp.system.erpsystem.dto.permission;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePermissionRequestDto {

    @NotNull(message = "startTime is required")
    @FutureOrPresent(message = "startTime cannot be in the past")
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    @FutureOrPresent(message = "endTime cannot be in the past")
    private LocalDateTime endTime;

    @Size(max = 1000, message = "Reason max length is 1000")
    private String reason;

    private Integer requestedById;
    private Integer orgId;
}

