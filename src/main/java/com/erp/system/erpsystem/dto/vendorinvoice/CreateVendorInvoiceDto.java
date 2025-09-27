package com.erp.system.erpsystem.dto.vendorinvoice;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateVendorInvoiceDto {
    private BigDecimal invoiceAmount;
    private String taxDetails;
    private LocalDate invoiceDate;
    private Integer vendorId;
    private Integer poId;
    private Integer grnId;
}
