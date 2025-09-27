package com.erp.system.erpsystem.dto.vendorinvoice;

import com.erp.system.erpsystem.model.enums.InvoiceStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class VendorInvoiceDto {
    private Integer invoiceId;
    private BigDecimal invoiceAmount;
    private String taxDetails;
    private LocalDate invoiceDate;
    private InvoiceStatus status;
    private Integer vendorId;
    private Integer poId;
    private Integer grnId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

