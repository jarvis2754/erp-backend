package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.purchaserequisition.CreatePurchaseRequisitionDto;
import com.erp.system.erpsystem.dto.purchaserequisition.PurchaseRequisitionDto;
import com.erp.system.erpsystem.dto.purchaserequisition.UpdatePRStatusDto;
import com.erp.system.erpsystem.mapper.PurchaseRequisitionMapper;
import com.erp.system.erpsystem.model.Organization;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.model.procurement.PurchaseRequisition;
import com.erp.system.erpsystem.repository.PurchaseRequisitionRepository;
import com.erp.system.erpsystem.repository.OrganizationRepository;
import com.erp.system.erpsystem.repository.UserRepository;
import com.erp.system.erpsystem.utils.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PurchaseRequisitionService {

    private final PurchaseRequisitionRepository prRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository orgRepository;
    private final PurchaseRequisitionMapper mapper;
    private final JwtUtil jwtUtil;

    public PurchaseRequisitionService(PurchaseRequisitionRepository prRepository,
                                      UserRepository userRepository,
                                      OrganizationRepository orgRepository,
                                      PurchaseRequisitionMapper mapper,
                                      JwtUtil jwtUtil) {
        this.prRepository = prRepository;
        this.userRepository = userRepository;
        this.orgRepository = orgRepository;
        this.mapper = mapper;
        this.jwtUtil = jwtUtil;
    }

    // --- CREATE PR ---
    public PurchaseRequisitionDto createPR(String token, CreatePurchaseRequisitionDto dto) {
        User requestedBy = getUserFromToken(token);
        Organization org = getOrgFromToken(token);

        // Only allowed departments can create PR
        if (!canCreatePR(requestedBy)) {
            throw new RuntimeException("Unauthorized: Only Procurement or Finance can create PRs");
        }

        PurchaseRequisition pr = mapper.toEntity(dto, requestedBy, org);
        pr = prRepository.save(pr);
        return mapper.toDto(pr);
    }

    // --- GET ALL PRs ---
    public List<PurchaseRequisitionDto> getAllPRs(Integer orgId, String token) {
        User user = getUserFromToken(token);

        if (orgId == null) {
            orgId = jwtUtil.extractOrgId(token);
        }

        if (canViewAllPRs(user)) {
            return prRepository.findByOrgOrgId(orgId)
                    .stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
        }

        // Otherwise, only show PRs requested by the user
        return prRepository.findByRequestedBy(user)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    // --- GET PR BY ID ---
    public PurchaseRequisitionDto getPRById(String token, Integer prId) {
        User user = getUserFromToken(token);

        PurchaseRequisition pr = prRepository.findById(prId)
                .orElseThrow(() -> new RuntimeException("PR not found"));

        if (!canViewPR(user, pr)) {
            throw new RuntimeException("You do not have permission to view this PR");
        }

        return mapper.toDto(pr);
    }

    // --- UPDATE PR STATUS ---
    public PurchaseRequisitionDto updateStatus(Integer prId, UpdatePRStatusDto dto, String token) {
        User user = getUserFromToken(token);

        if (!canApprovePR(user)) {
            throw new RuntimeException("You do not have permission to approve/reject/cancel PRs");
        }

        PurchaseRequisition pr = prRepository.findById(prId)
                .orElseThrow(() -> new RuntimeException("PR not found"));

        pr.setStatus(dto.getStatus());

        if (dto.getApprovedById() != null) {
            User approvedBy = userRepository.findById(dto.getApprovedById())
                    .orElseThrow(() -> new RuntimeException("Approver not found"));
            pr.setApprovedBy(approvedBy);
        } else {
            pr.setApprovedBy(user); // default to current user
        }

        return mapper.toDto(prRepository.save(pr));
    }

    // ================= Helper Methods =================

    private User getUserFromToken(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Organization getOrgFromToken(String token) {
        Integer orgId = jwtUtil.extractOrgId(token);
        return orgRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

    // Departments allowed to create PR
    private boolean canCreatePR(User user) {
        return user.getDepartment() == Department.PROCUREMENT
                || user.getDepartment() == Department.FINANCE;
    }

    // Departments/positions allowed to view all PRs
    private boolean canViewAllPRs(User user) {
        return user.getDepartment() == Department.PROCUREMENT
                || user.getDepartment() == Department.FINANCE
                || user.getDepartment() == Department.ADMINISTRATION;
    }

    // Check if a user can view a specific PR
    private boolean canViewPR(User user, PurchaseRequisition pr) {
        return canViewAllPRs(user) || pr.getRequestedBy().equals(user);
    }

    // Only manager+ positions in allowed departments can approve PR
    private boolean canApprovePR(User user) {
        return (user.getDepartment() == Department.PROCUREMENT
                || user.getDepartment() == Department.FINANCE
                || user.getDepartment() == Department.ADMINISTRATION)
                && isManagerOrAbove(user.getPosition());
    }

    // Check if user's position allows approval
    private boolean isManagerOrAbove(Position position) {
        return switch (position) {
            case LEAD, MANAGER, SENIOR_MANAGER, DIRECTOR,
                 VICE_PRESIDENT, PRESIDENT, CXO, CEO -> true;
            default -> false;
        };
    }
}
