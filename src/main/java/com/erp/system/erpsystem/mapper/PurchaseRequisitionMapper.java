package com.erp.system.erpsystem.mapper;

import com.erp.system.erpsystem.dto.purchaserequisition.CreatePurchaseRequisitionDto;
import com.erp.system.erpsystem.dto.purchaserequisition.PurchaseRequisitionDto;
import com.erp.system.erpsystem.model.Organization;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.PRStatus;
import com.erp.system.erpsystem.model.procurement.PurchaseRequisition;
import org.springframework.stereotype.Component;

@Component
public class PurchaseRequisitionMapper {

    public PurchaseRequisitionDto toDto(PurchaseRequisition pr) {
        if (pr == null) return null;
        return PurchaseRequisitionDto.builder()
                .prId(pr.getPrId())
                .itemName(pr.getItemName())
                .quantity(pr.getQuantity())
                .estimatedPrice(pr.getEstimatedPrice())
                .costCenter(pr.getCostCenter())
                .needByDate(pr.getNeedByDate())
                .priority(pr.getPriority())
                .description(pr.getDescription())
                .status(pr.getStatus())
                .createdAt(pr.getCreatedAt())
                .updatedAt(pr.getUpdatedAt())
                .requestedById(pr.getRequestedBy() != null ? pr.getRequestedBy().getUserId() : null)
                .approvedById(pr.getApprovedBy() != null ? pr.getApprovedBy().getUserId() : null)
                .orgId(pr.getOrg() != null ? pr.getOrg().getOrgId() : null)
                .build();
    }

    public PurchaseRequisition toEntity(CreatePurchaseRequisitionDto dto, User requestedBy, Organization org) {
        if (dto == null) return null;
        PurchaseRequisition pr = new PurchaseRequisition();
        pr.setItemName(dto.getItemName());
        pr.setQuantity(dto.getQuantity());
        pr.setEstimatedPrice(dto.getEstimatedPrice());
        pr.setCostCenter(dto.getCostCenter());
        pr.setNeedByDate(dto.getNeedByDate());
        pr.setPriority(dto.getPriority());
        pr.setDescription(dto.getDescription());
        pr.setRequestedBy(requestedBy);
        pr.setOrg(org);
        pr.setStatus(PRStatus.PENDING);
        return pr;
    }
}

