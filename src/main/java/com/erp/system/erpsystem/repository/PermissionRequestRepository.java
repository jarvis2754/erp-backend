package com.erp.system.erpsystem.repository;

import com.erp.system.erpsystem.model.Permission;
import com.erp.system.erpsystem.model.PermissionRequest;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.PermissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PermissionRequestRepository extends JpaRepository<PermissionRequest, Integer> {

    // paging for org-level listing
    Page<PermissionRequest> findByOrganization_OrgId(Integer orgId, Pageable pageable);

    Page<PermissionRequest> findByOrganization_OrgIdAndStatus(Integer orgId, PermissionStatus status, Pageable pageable);

    // all pending (HR)
    List<PermissionRequest> findByStatus(PermissionStatus status);

    // pending for a department (managers)
    List<PermissionRequest> findByRequestedBy_DepartmentAndStatus(Department department, PermissionStatus status);

    // by user
    List<PermissionRequest> findByRequestedBy_UserId(Integer userId);
    List<PermissionRequest> findByRequestedBy_UserIdOrderByCreatedAtDesc(Integer userId);
    Page<PermissionRequest> findByOrganization_OrgCodeOrderByCreatedAtDesc(String orgCode, Pageable pageable);
    Page<PermissionRequest> findByOrganization_OrgCodeAndStatusOrderByCreatedAtDesc(String orgId, PermissionStatus status, Pageable pageable);


}


