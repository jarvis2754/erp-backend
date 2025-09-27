package com.erp.system.erpsystem.repository;

import com.erp.system.erpsystem.model.procurement.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<Vendor,Integer> {
}
