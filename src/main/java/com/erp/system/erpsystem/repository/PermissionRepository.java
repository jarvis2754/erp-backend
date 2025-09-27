package com.erp.system.erpsystem.repository;

import com.erp.system.erpsystem.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    List<Permission> findByUser_UserId(Integer id);
}
