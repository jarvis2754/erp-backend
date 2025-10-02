package com.erp.system.erpsystem.repository;

import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.Position;
import org.springframework.data.domain.Pageable; // <-- Correct import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String userName);

    List<User> findByUserNameContainingIgnoreCase(String keyword);

    Optional<User> findByUuId(String uuid);

    @Query("SELECT u.uuId FROM User u WHERE u.uuId LIKE :prefix% ORDER BY u.uuId DESC")
    List<String> findTopByPrefix(String prefix, Pageable pageable);

    List<User> findByOrganization_OrgId(Integer orgId);

    Optional<User> findByUserName(String userName);
    int countByOrganizationOrgId(Integer orgId);

    long countByPositionLessThanAndOrganizationOrgIdAndJoiningDateLessThanEqual(Position position, Integer orgId, LocalDate joiningDate);




}
