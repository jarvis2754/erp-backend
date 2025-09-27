package com.erp.system.erpsystem.dto.purchaseorder;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreatePurchaseOrderDto {
    private BigDecimal totalPrice;
    private String taxes;
    private String deliveryTerms;
    private Integer vendorId;
    private Integer prId;
    private Integer orgId;
}

