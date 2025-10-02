package com.erp.system.erpsystem.repository;

import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.PRStatus;
import com.erp.system.erpsystem.model.enums.PermissionStatus;
import com.erp.system.erpsystem.model.procurement.PurchaseRequisition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRequisitionRepository extends JpaRepository<PurchaseRequisition, Integer> {
    List<PurchaseRequisition> findByOrgOrgId(Integer orgId);

    List<PurchaseRequisition> findByRequestedBy(User user);

    int countByOrgOrgIdAndStatus(Integer orgId, PRStatus status);
}

