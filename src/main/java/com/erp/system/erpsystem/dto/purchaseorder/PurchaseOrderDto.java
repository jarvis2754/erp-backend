package com.erp.system.erpsystem.dto.purchaseorder;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseOrderDto {
    private Integer poId;
    private BigDecimal totalPrice;
    private String taxes;
    private String deliveryTerms;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String vendorName;
    private String requisitionItem;
    private String orgName;
}

