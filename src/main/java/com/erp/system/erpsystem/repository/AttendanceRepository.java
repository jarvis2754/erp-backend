package com.erp.system.erpsystem.repository;

import com.erp.system.erpsystem.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance,Integer> {
    List<Attendance> findByUser_UserIdOrderByDateDesc(Integer id);
    Optional<Attendance> findByUser_UserIdAndDate(Integer id, LocalDate date);

}
