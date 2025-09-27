package com.erp.system.erpsystem.dto.leave;

import com.erp.system.erpsystem.model.enums.LeaveType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLeaveRequestDto {

    @NotNull(message = "startDate is required")
    @FutureOrPresent(message = "startDate cannot be in the past")
    private LocalDate startDate;

    @NotNull(message = "endDate is required")
    @FutureOrPresent(message = "endDate cannot be in the past")
    private LocalDate endDate;

    @Size(max = 2000, message = "Reason max length is 2000")
    private String reason;

    @NotNull(message = "leaveType is required")
    private LeaveType leaveType;

    private Integer requestedById;
    private Integer orgId;
}

