package com.erp.system.erpsystem.repository;


import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.procurement.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {
    List<PurchaseOrder> findByPurchaseRequisition_RequestedBy(User user);
    List<PurchaseOrder> findByOrg_OrgId(Integer id);
}

