package com.erp.system.erpsystem.dto.purchaserequisition;

import com.erp.system.erpsystem.model.enums.PRStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequisitionDto {
    private Integer prId;
    private String itemName;
    private Integer quantity;
    private BigDecimal estimatedPrice;
    private String costCenter;
    private LocalDate needByDate;
    private String priority;
    private String description;
    private PRStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer requestedById;
    private Integer approvedById;
    private Integer orgId;
}

