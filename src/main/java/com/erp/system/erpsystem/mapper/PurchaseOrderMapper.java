package com.erp.system.erpsystem.mapper;

import com.erp.system.erpsystem.dto.purchaseorder.PurchaseOrderDto;
import com.erp.system.erpsystem.dto.purchaseorder.CreatePurchaseOrderDto;
import com.erp.system.erpsystem.model.enums.POStatus;
import com.erp.system.erpsystem.model.procurement.PurchaseOrder;
import com.erp.system.erpsystem.model.procurement.Vendor;
import com.erp.system.erpsystem.model.procurement.PurchaseRequisition;
import com.erp.system.erpsystem.model.Organization;

public class PurchaseOrderMapper {

    public static PurchaseOrderDto toDto(PurchaseOrder po) {
        PurchaseOrderDto dto = new PurchaseOrderDto();
        dto.setPoId(po.getPoId());
        dto.setTotalPrice(po.getTotalPrice());
        dto.setTaxes(po.getTaxes());
        dto.setDeliveryTerms(po.getDeliveryTerms());
        dto.setStatus(po.getStatus().name());
        dto.setCreatedAt(po.getCreatedAt());
        dto.setUpdatedAt(po.getUpdatedAt());
        dto.setVendorName(po.getVendor() != null ? po.getVendor().getName() : null);
        dto.setRequisitionItem(po.getPurchaseRequisition() != null ? po.getPurchaseRequisition().getItemName() : null);
        dto.setOrgName(po.getOrg() != null ? po.getOrg().getOrgName() : null);
        return dto;
    }

    public static PurchaseOrder toEntity(CreatePurchaseOrderDto dto, Vendor vendor, PurchaseRequisition pr, Organization org) {
        return PurchaseOrder.builder()
                .totalPrice(dto.getTotalPrice())
                .taxes(dto.getTaxes())
                .deliveryTerms(dto.getDeliveryTerms())
                .vendor(vendor)
                .purchaseRequisition(pr)
                .org(org)
                .status(POStatus.OPEN)
                .build();
    }
}

