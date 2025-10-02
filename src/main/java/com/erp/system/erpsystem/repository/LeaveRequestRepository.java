package com.erp.system.erpsystem.repository;


import com.erp.system.erpsystem.model.LeaveRequest;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;


@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {


    List<LeaveRequest> findByOrganization_OrgId(Integer orgId);


    Page<LeaveRequest> findByOrganization_OrgCodeOrderByCreatedAtDesc(String orgId, Pageable pageable);


    List<LeaveRequest> findByRequestedBy_UserId(Integer userId);
    List<LeaveRequest> findByRequestedBy_UserIdOrderByCreatedAtDesc(Integer userId);


    List<LeaveRequest> findByStatus(LeaveStatus status);

    Page<LeaveRequest> findByOrganization_OrgCodeAndStatusOrderByCreatedAtDesc(String orgId, LeaveStatus status, Pageable pageable);

    List<LeaveRequest> findByRequestedBy_DepartmentAndStatus(Department department, LeaveStatus status);
    List<LeaveRequest> findByRequestedByUserIdAndStatusAndStartDateAfter(
            Integer userId, LeaveStatus status, LocalDate startDate
    );

    int countByOrganizationOrgIdAndStatus(Integer orgId,LeaveStatus status);
}