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

    public PurchaseRequisitionDto createPR(String token, CreatePurchaseRequisitionDto dto) {

        User requestedBy = userRepository.findById(jwtUtil.extractUserId(token))
                .orElseThrow(() -> new RuntimeException("User not found"));
        Organization org = orgRepository.findById(jwtUtil.extractOrgId(token))
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        PurchaseRequisition pr = mapper.toEntity(dto, requestedBy, org);
        pr = prRepository.save(pr);
        return mapper.toDto(pr);
    }

    public List<PurchaseRequisitionDto> getAllPRs(Integer orgId, String token) {

        User user = userRepository.findById(jwtUtil.extractUserId(token))
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (orgId == null) {
            orgId = jwtUtil.extractOrgId(token);
        }

        // Only Procurement managers and above can see all PRs
        if (user.getDepartment() == Department.PROCUREMENT && isManagerOrAbove(user.getPosition())) {
            return prRepository.findByOrgOrgId(orgId)
                    .stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
        }

        // Otherwise, only PRs requested by the user
        return prRepository.findByRequestedBy(user)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public PurchaseRequisitionDto getPRById(String token, Integer prId) {
        User user = userRepository.findById(jwtUtil.extractUserId(token))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDepartment() != Department.PROCUREMENT || !isManagerOrAbove(user.getPosition())) {
            throw new RuntimeException("You do not have permission to update PR status");
        }
        return prRepository.findById(prId)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("PR not found"));
    }

    public PurchaseRequisitionDto updateStatus(Integer prId, UpdatePRStatusDto dto, String token) {
        Integer userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only Procurement managers and above can approve/reject/cancel
        if (user.getDepartment() != Department.PROCUREMENT || !isManagerOrAbove(user.getPosition())) {
            throw new RuntimeException("You do not have permission to update PR status");
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

    // Helper to check if user's position allows approval
    private boolean isManagerOrAbove(Position position) {
        return switch (position) {
            case MANAGER, SENIOR_MANAGER, DIRECTOR, VICE_PRESIDENT, PRESIDENT, CXO, CEO -> true;
            default -> false;
        };
    }
}
