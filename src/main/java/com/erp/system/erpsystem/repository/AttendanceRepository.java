package com.erp.system.erpsystem.repository;

import com.erp.system.erpsystem.model.Attendance;
import com.erp.system.erpsystem.model.Organization;
import com.erp.system.erpsystem.model.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance,Integer> {

    Optional<Attendance> findByUser_UserIdAndDate(Integer id, LocalDate date);
    int countByDateAndStatusAndUserOrganization(LocalDate date, AttendanceStatus status, Organization organization);
    Page<Attendance> findByUser_UserIdOrderByDateDesc(Integer userId, Pageable pageable);
    List<Attendance> findByUser_UserIdAndDateBetween(Integer userId, LocalDate start, LocalDate end);
}
