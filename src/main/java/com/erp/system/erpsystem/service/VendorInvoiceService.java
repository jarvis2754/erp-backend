package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.vendorinvoice.CreateVendorInvoiceDto;
import com.erp.system.erpsystem.dto.vendorinvoice.VendorInvoiceDto;
import com.erp.system.erpsystem.mapper.VendorInvoiceMapper;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.InvoiceStatus;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.model.procurement.GoodsReceipt;
import com.erp.system.erpsystem.model.procurement.PurchaseOrder;
import com.erp.system.erpsystem.model.procurement.Vendor;
import com.erp.system.erpsystem.model.procurement.VendorInvoice;
import com.erp.system.erpsystem.repository.GoodsReceiptRepository;
import com.erp.system.erpsystem.repository.PurchaseOrderRepository;
import com.erp.system.erpsystem.repository.UserRepository;
import com.erp.system.erpsystem.repository.VendorInvoiceRepository;
import com.erp.system.erpsystem.repository.VendorRepository;
import com.erp.system.erpsystem.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorInvoiceService {

    private final VendorInvoiceRepository viRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseOrderRepository poRepository;
    private final GoodsReceiptRepository grRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private User getUserFromToken(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Integer getOrgIdFromToken(String token) {
        return jwtUtil.extractOrgId(token);
    }

    private boolean isFinanceOrManager(User user) {
        Department dept = user.getDepartment();
        Position pos = user.getPosition();
        return dept == Department.FINANCE || pos.ordinal() >= Position.MANAGER.ordinal();
    }

    private boolean canViewInvoices(User user) {
        Department dept = user.getDepartment();
        Position pos = user.getPosition();
        return dept == Department.FINANCE || dept == Department.PROCUREMENT || pos.ordinal() >= Position.MANAGER.ordinal();
    }

    // Create Invoice
    public VendorInvoiceDto create(String token, CreateVendorInvoiceDto dto) {
        User user = getUserFromToken(token);
        if (!isFinanceOrManager(user)) throw new RuntimeException("Unauthorized: Only finance/manager+ can create invoices");

        Vendor vendor = vendorRepository.findById(dto.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        PurchaseOrder po = poRepository.findById(dto.getPoId())
                .orElseThrow(() -> new RuntimeException("PO not found"));
        GoodsReceipt gr = grRepository.findById(dto.getGrnId())
                .orElseThrow(() -> new RuntimeException("Goods Receipt not found"));

        VendorInvoice vi = VendorInvoiceMapper.toEntity(dto, vendor, po, gr);
        vi.setStatus(InvoiceStatus.PENDING);
        return VendorInvoiceMapper.toDto(viRepository.save(vi));
    }

    // Get all invoices for org
    public List<VendorInvoiceDto> getAll(String token) {
        User user = getUserFromToken(token);
        if (!canViewInvoices(user)) throw new RuntimeException("Unauthorized: Cannot view invoices");

        Integer orgId = getOrgIdFromToken(token);
        return viRepository.findByPurchaseOrder_Org_OrgId(orgId)
                .stream()
                .map(VendorInvoiceMapper::toDto)
                .collect(Collectors.toList());
    }

    // Get invoice by ID
    public VendorInvoiceDto getById(String token, Integer id) {
        User user = getUserFromToken(token);
        if (!canViewInvoices(user)) throw new RuntimeException("Unauthorized: Cannot view invoice");

        VendorInvoice vi = viRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor Invoice not found"));
        return VendorInvoiceMapper.toDto(vi);
    }

    // Update invoice status (PATCH)
    public VendorInvoiceDto updateStatus(String token, Integer id, String status) {
        User user = getUserFromToken(token);
        if (!isFinanceOrManager(user)) throw new RuntimeException("Unauthorized: Only finance/manager+ can update invoice");

        VendorInvoice vi = viRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor Invoice not found"));
        vi.setStatus(Enum.valueOf(com.erp.system.erpsystem.model.enums.InvoiceStatus.class, status.toUpperCase()));
        return VendorInvoiceMapper.toDto(viRepository.save(vi));
    }
}

