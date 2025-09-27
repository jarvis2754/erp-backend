package com.erp.system.erpsystem.mapper;

import com.erp.system.erpsystem.dto.vendorinvoice.CreateVendorInvoiceDto;
import com.erp.system.erpsystem.dto.vendorinvoice.VendorInvoiceDto;
import com.erp.system.erpsystem.model.procurement.GoodsReceipt;
import com.erp.system.erpsystem.model.procurement.PurchaseOrder;
import com.erp.system.erpsystem.model.procurement.VendorInvoice;
import com.erp.system.erpsystem.model.procurement.Vendor;

public class VendorInvoiceMapper {

    public static VendorInvoiceDto toDto(VendorInvoice vi) {
        VendorInvoiceDto dto = new VendorInvoiceDto();
        dto.setInvoiceId(vi.getInvoiceId());
        dto.setInvoiceAmount(vi.getInvoiceAmount());
        dto.setTaxDetails(vi.getTaxDetails());
        dto.setInvoiceDate(vi.getInvoiceDate());
        dto.setStatus(vi.getStatus());
        dto.setVendorId(vi.getVendor() != null ? vi.getVendor().getVendorId() : null);
        dto.setPoId(vi.getPurchaseOrder() != null ? vi.getPurchaseOrder().getPoId() : null);
        dto.setGrnId(vi.getGoodsReceipt() != null ? vi.getGoodsReceipt().getGrnId() : null);
        dto.setCreatedAt(vi.getCreatedAt());
        dto.setUpdatedAt(vi.getUpdatedAt());
        return dto;
    }

    public static VendorInvoice toEntity(CreateVendorInvoiceDto dto, Vendor vendor, PurchaseOrder po, GoodsReceipt gr) {
        return VendorInvoice.builder()
                .invoiceAmount(dto.getInvoiceAmount())
                .taxDetails(dto.getTaxDetails())
                .invoiceDate(dto.getInvoiceDate())
                .vendor(vendor)
                .purchaseOrder(po)
                .goodsReceipt(gr)
                .build();
    }
}

