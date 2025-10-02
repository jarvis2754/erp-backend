package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.goodsreceipt.CreateGoodsReceiptDto;
import com.erp.system.erpsystem.dto.goodsreceipt.GoodsReceiptDto;
import com.erp.system.erpsystem.mapper.GoodsReceiptMapper;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.model.procurement.GoodsReceipt;
import com.erp.system.erpsystem.model.procurement.PurchaseOrder;
import com.erp.system.erpsystem.repository.GoodsReceiptRepository;
import com.erp.system.erpsystem.repository.PurchaseOrderRepository;
import com.erp.system.erpsystem.repository.UserRepository;
import com.erp.system.erpsystem.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository poRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /** --- Authorization Helpers --- **/

    private User getCurrentUser(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isOperationsOrManager(User user) {
        Department dept = user.getDepartment();
        Position pos = user.getPosition();
        return (dept == Department.PROCUREMENT
                || dept == Department.FINANCE
                || dept == Department.OPERATIONS)
                && pos.ordinal() >= Position.LEAD.ordinal();
    }

    private boolean isOperations(User user) {
        Department dept = user.getDepartment();
        Position pos = user.getPosition();
        return (dept == Department.PROCUREMENT
                || dept == Department.FINANCE
                || dept == Department.OPERATIONS);

    }

    private boolean canApprove(User user) {
        Department dept = user.getDepartment();
        Position pos = user.getPosition();
        return (dept == Department.PROCUREMENT
                || dept == Department.OPERATIONS)
                && pos.ordinal() >= Position.MANAGER.ordinal();
    }

    private boolean canView(User user, GoodsReceipt gr) {
        return isOperations(user)
                || (gr.getPurchaseOrder() != null
                && gr.getPurchaseOrder().getOrg() != null
                && user.getOrganization().getOrgId().equals(gr.getPurchaseOrder().getOrg().getOrgId()));
    }

    /** --- Service Methods --- **/

    public List<GoodsReceiptDto> getAll(String token) {
        User user = getCurrentUser(token);

        if (!isOperations(user) && !(user.getDepartment() == Department.ADMINISTRATION)) {
            throw new RuntimeException("Unauthorized: Cannot view Goods Receipts");
        }

        return goodsReceiptRepository.findByPurchaseOrder_Org_OrgId(user.getOrganization().getOrgId())
                .stream()
                .map(GoodsReceiptMapper::toDto)
                .collect(Collectors.toList());
    }

    public GoodsReceiptDto getById(Integer id, String token) {
        GoodsReceipt gr = goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goods Receipt not found"));
        User user = getCurrentUser(token);

        if (!(canView(user, gr) || user.getDepartment() == Department.ADMINISTRATION )) {
            throw new RuntimeException("Unauthorized: Cannot view this Goods Receipt");
        }

        return GoodsReceiptMapper.toDto(gr);
    }

    public GoodsReceiptDto create(CreateGoodsReceiptDto dto, String token) {
        User user = getCurrentUser(token);

        if (!isOperationsOrManager(user)) {
            throw new RuntimeException("Unauthorized: Only Operations/Manager+ can create Goods Receipts");
        }

        PurchaseOrder po = poRepository.findById(dto.getPoId())
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        GoodsReceipt gr = GoodsReceiptMapper.toEntity(dto, po);
        return GoodsReceiptMapper.toDto(goodsReceiptRepository.save(gr));
    }

    public GoodsReceiptDto update(Integer id, CreateGoodsReceiptDto dto, String token) {
        User user = getCurrentUser(token);

        if (!canApprove(user)) {
            throw new RuntimeException("Unauthorized: Only Operations/Manager+ can update Goods Receipts");
        }

        GoodsReceipt gr = goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goods Receipt not found"));

        if (dto.getReceivedDate() != null) gr.setReceivedDate(dto.getReceivedDate());
        if (dto.getReceivedQuantity() != null) gr.setReceivedQuantity(dto.getReceivedQuantity());
        if (dto.getQcStatus() != null) gr.setQcStatus(dto.getQcStatus());
        if (dto.getCondition() != null) gr.setCondition(dto.getCondition());
        if (dto.getRemarks() != null) gr.setRemarks(dto.getRemarks());

        return GoodsReceiptMapper.toDto(goodsReceiptRepository.save(gr));
    }
}
