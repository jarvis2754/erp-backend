package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.purchaseorder.CreatePurchaseOrderDto;
import com.erp.system.erpsystem.dto.purchaseorder.PurchaseOrderDto;
import com.erp.system.erpsystem.mapper.PurchaseOrderMapper;
import com.erp.system.erpsystem.model.*;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.POStatus;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.model.procurement.PurchaseOrder;
import com.erp.system.erpsystem.model.procurement.PurchaseRequisition;
import com.erp.system.erpsystem.model.procurement.Vendor;
import com.erp.system.erpsystem.repository.*;
import com.erp.system.erpsystem.utils.JwtUtil;
import com.erp.system.erpsystem.utils.PurchaseOrderUtils;

import com.lowagie.text.*;
import com.lowagie.text.Font;

import com.lowagie.text.pdf.PdfPTable;

import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseRequisitionRepository prRepository;
    private final OrganizationRepository orgRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /** --- Helper Methods --- **/

    private User getCurrentUser(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isManagerOrAbove(Position position) {
        return switch (position) {
            case LEAD, MANAGER, SENIOR_MANAGER, DIRECTOR, VICE_PRESIDENT, PRESIDENT, CXO, CEO -> true;
            default -> false;
        };
    }

    private boolean isTopManagement(Position position) {
        return switch (position) {
            case DIRECTOR, VICE_PRESIDENT, PRESIDENT, CXO, CEO -> true;
            default -> false;
        };
    }

    private boolean canCreatePO(User user) {
        return user.getDepartment() == Department.PROCUREMENT &&
                (user.getPosition() == Position.EXECUTIVE
                        || user.getPosition() == Position.SENIOR_EXECUTIVE
                        || isManagerOrAbove(user.getPosition()));
    }

    private boolean canViewPO(User user, PurchaseOrder po) {
        return user.getDepartment() == Department.PROCUREMENT
                || user.getDepartment() == Department.FINANCE
                || (user.getDepartment() == Department.OPERATIONS && isManagerOrAbove(user.getPosition()))
                || isTopManagement(user.getPosition())
                || (po.getPurchaseRequisition() != null &&
                po.getPurchaseRequisition().getRequestedBy().getUserId().equals(user.getUserId()));
    }

    private boolean canApprovePO(User user) {
        return (user.getDepartment() == Department.PROCUREMENT
                || user.getDepartment() == Department.FINANCE)&& isManagerOrAbove(user.getPosition());
    }

    /** --- Service Methods --- **/

    public List<PurchaseOrderDto> getAllForUser(String token, Integer orgId) {
        User user = getCurrentUser(token);
        if (orgId == null) orgId = jwtUtil.extractOrgId(token);

        if (user.getDepartment() == Department.PROCUREMENT
                || user.getDepartment() == Department.FINANCE
                || (user.getDepartment() == Department.ADMINISTRATION && isManagerOrAbove(user.getPosition()))
                || isTopManagement(user.getPosition())) {
            return purchaseOrderRepository.findByOrg_OrgId(orgId)
                    .stream().map(PurchaseOrderMapper::toDto).toList();
        }

        return purchaseOrderRepository.findByPurchaseRequisition_RequestedBy(user)
                .stream().map(PurchaseOrderMapper::toDto).toList();
    }

    public PurchaseOrderDto getById(Integer id, String token) {
        User user = getCurrentUser(token);
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        if (!canViewPO(user, po)) {
            throw new RuntimeException("Unauthorized: cannot view this PO");
        }

        return PurchaseOrderMapper.toDto(po);
    }

    public PurchaseOrderDto create(CreatePurchaseOrderDto dto, String token) {
        User user = getCurrentUser(token);
        if (!canCreatePO(user)) {
            throw new RuntimeException("Unauthorized: only Procurement Exec+ can create POs");
        }

        Vendor vendor = vendorRepository.findById(dto.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        PurchaseRequisition pr = prRepository.findById(dto.getPrId())
                .orElseThrow(() -> new RuntimeException("Purchase Requisition not found"));
        Organization org = orgRepository.findById(jwtUtil.extractOrgId(token))
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        PurchaseOrder po = PurchaseOrderMapper.toEntity(dto, vendor, pr, org);
        return PurchaseOrderMapper.toDto(purchaseOrderRepository.save(po));
    }

    public PurchaseOrderDto updateStatus(Integer id, String status, String token) {
        User user = getCurrentUser(token);
        if (!canApprovePO(user)) {
            throw new RuntimeException("Unauthorized: cannot approve/update PO");
        }

        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));
        try {
            po.setStatus(POStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid PO status: " + status);
        }

        return PurchaseOrderMapper.toDto(purchaseOrderRepository.save(po));
    }

    public byte[] generatePdf(Integer id, String token) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));
        User user = getCurrentUser(token);

        if (!canViewPO(user, po)) {
            throw new RuntimeException("Unauthorized: cannot generate PDF for this PO");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // --- Title ---
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph("Purchase Order Invoice", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font orgFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph orgName = new Paragraph(po.getOrg().getOrgName(), orgFont);
            orgName.setAlignment(Element.ALIGN_CENTER);
            document.add(orgName);

            document.add(new Paragraph(" ")); // empty line

            // --- PO & Vendor Info Table ---
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(20);
            infoTable.setSpacingAfter(20);
            infoTable.setWidths(new int[]{1, 2});

            infoTable.addCell(PurchaseOrderUtils.getBorderedCell("PO ID:", true));
            infoTable.addCell(PurchaseOrderUtils.getBorderedCell(po.getPoId().toString(), false));

            infoTable.addCell(PurchaseOrderUtils.getBorderedCell("Vendor:", true));
            infoTable.addCell(PurchaseOrderUtils.getBorderedCell(po.getVendor().getName(), false));

            infoTable.addCell(PurchaseOrderUtils.getBorderedCell("Organization:", true));
            infoTable.addCell(PurchaseOrderUtils.getBorderedCell(po.getOrg().getOrgName(), false));

            infoTable.addCell(PurchaseOrderUtils.getBorderedCell("Created At:", true));
            infoTable.addCell(PurchaseOrderUtils.getBorderedCell(po.getCreatedAt().toString(), false));

            infoTable.addCell(PurchaseOrderUtils.getBorderedCell("Delivery Terms:", true));
            infoTable.addCell(PurchaseOrderUtils.getBorderedCell(po.getDeliveryTerms(), false));

            infoTable.addCell(PurchaseOrderUtils.getBorderedCell("Status:", true));
            infoTable.addCell(PurchaseOrderUtils.getBorderedCell(po.getStatus().toString(), false));

            document.add(infoTable);

            // --- Requisition Items Table ---
            if (po.getPurchaseRequisition() != null) {
                PdfPTable reqTable = new PdfPTable(3);
                reqTable.setWidthPercentage(100);
                reqTable.setSpacingBefore(10);
                reqTable.setWidths(new int[]{2, 5, 2});

                reqTable.addCell(PurchaseOrderUtils.getHeaderCell("Item"));
                reqTable.addCell(PurchaseOrderUtils.getHeaderCell("Requested By"));
                reqTable.addCell(PurchaseOrderUtils.getHeaderCell("Quantity"));

                reqTable.addCell(PurchaseOrderUtils.getBorderedCell(po.getPurchaseRequisition().getItemName(), false));
                reqTable.addCell(PurchaseOrderUtils.getBorderedCell(po.getPurchaseRequisition().getRequestedBy().getUserName(), false));
                reqTable.addCell(PurchaseOrderUtils.getBorderedCell(po.getPurchaseRequisition().getQuantity().toString(), false));

                document.add(reqTable);
            }

            // --- Pricing Summary Table ---
            PdfPTable priceTable = new PdfPTable(2);
            priceTable.setWidthPercentage(40);
            priceTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            priceTable.setSpacingBefore(30);

            priceTable.addCell(PurchaseOrderUtils.getBorderedCell("Total Price:", true));
            priceTable.addCell(PurchaseOrderUtils.getBorderedCell(po.getTotalPrice().toString(), false));

            priceTable.addCell(PurchaseOrderUtils.getBorderedCell("Taxes:", true));
            priceTable.addCell(PurchaseOrderUtils.getBorderedCell(po.getTaxes(), false));

            document.add(priceTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }

}
