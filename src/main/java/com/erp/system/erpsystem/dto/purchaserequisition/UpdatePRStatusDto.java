package com.erp.system.erpsystem.dto.purchaserequisition;

import com.erp.system.erpsystem.model.enums.PRStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePRStatusDto {
    private PRStatus status;
    private Integer approvedById; // Optional: who approved/rejected
}
