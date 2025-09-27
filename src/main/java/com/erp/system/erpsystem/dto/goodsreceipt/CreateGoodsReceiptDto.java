package com.erp.system.erpsystem.dto.goodsreceipt;

import com.erp.system.erpsystem.model.enums.ItemCondition;
import com.erp.system.erpsystem.model.enums.QCStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateGoodsReceiptDto {
    private Integer poId;
    private LocalDate receivedDate;
    private Integer receivedQuantity;
    private QCStatus qcStatus;
    private ItemCondition condition;
    private String remarks;
}

