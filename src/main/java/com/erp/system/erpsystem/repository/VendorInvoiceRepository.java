package com.erp.system.erpsystem.repository;

import com.erp.system.erpsystem.model.procurement.VendorInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorInvoiceRepository extends JpaRepository<VendorInvoice, Integer> {
    List<VendorInvoice> findByPurchaseOrder_Org_OrgId(Integer orgId);
}
