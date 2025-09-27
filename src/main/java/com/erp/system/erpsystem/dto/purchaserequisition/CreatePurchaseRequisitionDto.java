package com.erp.system.erpsystem.dto.purchaserequisition;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePurchaseRequisitionDto {
    private String itemName;
    private Integer quantity;
    private BigDecimal estimatedPrice;
    private String costCenter;
    private LocalDate needByDate;
    private String priority;
    private String description;
    private Integer requestedById;
    private Integer orgId;
}

