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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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


    private boolean isOperationsOrManager(User user) {
        Department dept = user.getDepartment();
        Position pos = user.getPosition();
        return dept == Department.OPERATIONS ||
                pos.ordinal() >= Position.MANAGER.ordinal();
    }

    public List<GoodsReceiptDto> getAll(String token) {
        User user = userRepository
                .findById(jwtUtil.extractUserId(token))
                .orElseThrow(()->new RuntimeException("User not found"));

        if (isOperationsOrManager(user) || user.getDepartment() == Department.PROCUREMENT) {
            return goodsReceiptRepository.findByPurchaseOrder_Org_OrgId(user.getOrganization().getOrgId())
                    .stream()
                    .map(GoodsReceiptMapper::toDto)
                    .collect(Collectors.toList());
        }

        throw new RuntimeException("Unauthorized: Cannot view GRs");
    }

    public GoodsReceiptDto getById(Integer id ,String token) {
        GoodsReceipt gr = goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goods Receipt not found"));
        User user = userRepository
                .findById(jwtUtil.extractUserId(token))
                .orElseThrow(()->new RuntimeException("User not found"));
        if (!isOperationsOrManager(user) && user.getDepartment() != Department.PROCUREMENT) {
            throw new RuntimeException("Unauthorized: Cannot view this GR");
        }
        return GoodsReceiptMapper.toDto(gr);
    }

    public GoodsReceiptDto create(CreateGoodsReceiptDto dto,String token) {
        User user = userRepository
                .findById(jwtUtil.extractUserId(token))
                .orElseThrow(()->new RuntimeException("User not found"));
        if (!isOperationsOrManager(user)) {
            throw new RuntimeException("Unauthorized: Only operations/manager+ can create GR");
        }

        PurchaseOrder po = poRepository.findById(dto.getPoId())
                .orElseThrow(() -> new RuntimeException("PO not found"));

        GoodsReceipt gr = GoodsReceiptMapper.toEntity(dto, po);
        return GoodsReceiptMapper.toDto(goodsReceiptRepository.save(gr));
    }

    public GoodsReceiptDto update(Integer id, CreateGoodsReceiptDto dto,String token) {
        User user = userRepository
                .findById(jwtUtil.extractUserId(token))
                .orElseThrow(()->new RuntimeException("User not found"));
        if (!isOperationsOrManager(user)) {
            throw new RuntimeException("Unauthorized: Only operations/manager+ can update GR");
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

