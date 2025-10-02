package com.erp.system.erpsystem.mapper;

import com.erp.system.erpsystem.dto.goodsreceipt.CreateGoodsReceiptDto;
import com.erp.system.erpsystem.dto.goodsreceipt.GoodsReceiptDto;
import com.erp.system.erpsystem.model.procurement.GoodsReceipt;
import com.erp.system.erpsystem.model.procurement.PurchaseOrder;

public class GoodsReceiptMapper {

    public static GoodsReceiptDto toDto(GoodsReceipt gr) {
        GoodsReceiptDto dto = new GoodsReceiptDto();
        dto.setGrnId(gr.getGrnId());
        dto.setPoId(gr.getPurchaseOrder().getPoId());
        dto.setReceivedDate(gr.getReceivedDate());
        dto.setReceivedQuantity(gr.getReceivedQuantity());
        dto.setQcStatus(gr.getQcStatus());
        dto.setCondition(gr.getCondition());
        dto.setRemarks(gr.getRemarks());
        dto.setVendorId(gr.getPurchaseOrder().getVendor().getVendorId());
        return dto;
    }

    public static GoodsReceipt toEntity(CreateGoodsReceiptDto dto, PurchaseOrder po) {
        return GoodsReceipt.builder()
                .purchaseOrder(po)
                .receivedDate(dto.getReceivedDate())
                .receivedQuantity(dto.getReceivedQuantity())
                .qcStatus(dto.getQcStatus())
                .condition(dto.getCondition())
                .remarks(dto.getRemarks())
                .build();
    }
}

