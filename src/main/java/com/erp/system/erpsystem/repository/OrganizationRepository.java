package com.erp.system.erpsystem.repository;

import com.erp.system.erpsystem.model.Organization;
import com.erp.system.erpsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    Optional<Organization> findByOrgCode(String code);
}
