package com.erp.system.erpsystem.repository;

import com.erp.system.erpsystem.model.procurement.GoodsReceipt;
import com.erp.system.erpsystem.model.procurement.PurchaseOrder;
import com.erp.system.erpsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Integer> {
    List<GoodsReceipt> findByPurchaseOrder_Org_OrgId(Integer orgId);
    List<GoodsReceipt> findByPurchaseOrder(PurchaseOrder po);
}

