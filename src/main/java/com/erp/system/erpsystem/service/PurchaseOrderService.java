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
import com.itextpdf.text.BaseColor;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
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
    private final JwtUtil jwtUtil; // custom service to parse claims

    private User getCurrentUser(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<PurchaseOrderDto> getAllForUser(String token, Integer orgId) {
        User user = getCurrentUser(token);
        if (orgId == null) {
            orgId = jwtUtil.extractOrgId(token);
        }
        if (user.getDepartment() == Department.PROCUREMENT || isManagerOrAbove(user.getPosition())) {
            return purchaseOrderRepository.findByOrg_OrgId(orgId)
                    .stream()
                    .map(PurchaseOrderMapper::toDto)
                    .toList();
        }

        // employee → only see their own PR-linked POs
        return purchaseOrderRepository.findByPurchaseRequisition_RequestedBy(user)
                .stream()
                .map(PurchaseOrderMapper::toDto)
                .toList();
    }

    public PurchaseOrderDto getById(Integer id, String token) {
        User user = userRepository
                .findById(jwtUtil.extractUserId(token))
                .orElseThrow(() -> new RuntimeException("User not found"));

        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));
        if (!(user.getDepartment() == Department.PROCUREMENT || isManagerOrAbove(user.getPosition()))
                || !(user.getUserId().equals(po.getPurchaseRequisition().getRequestedBy().getUserId()))) {
            throw new RuntimeException("Unauthorized: Only procurement or manager+ can create POs");
        }
        return PurchaseOrderMapper.toDto(po);
    }

    public PurchaseOrderDto create(CreatePurchaseOrderDto dto, String token) {
        User user = getCurrentUser(token);
        if (!(user.getDepartment() == Department.PROCUREMENT || isManagerOrAbove(user.getPosition()))) {
            throw new RuntimeException("Unauthorized: Only procurement or manager+ can create POs");
        }

        Vendor vendor = vendorRepository.findById(dto.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        PurchaseRequisition pr = prRepository.findById(dto.getPrId())
                .orElseThrow(() -> new RuntimeException("Purchase Requisition not found"));

        dto.setOrgId(jwtUtil.extractOrgId(token));

        Organization org = orgRepository.findById(dto.getOrgId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        PurchaseOrder po = PurchaseOrderMapper.toEntity(dto, vendor, pr, org);
        return PurchaseOrderMapper.toDto(purchaseOrderRepository.save(po));
    }

    public PurchaseOrderDto updateStatus(Integer id, String status, String token) {
        User user = getCurrentUser(token);
        if (!(user.getDepartment() == Department.PROCUREMENT || isManagerOrAbove(user.getPosition()))) {
            throw new RuntimeException("Unauthorized: Only procurement or manager+ can update POs");
        }

        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));
        po.setStatus(POStatus.valueOf(status.toUpperCase()));
        return PurchaseOrderMapper.toDto(purchaseOrderRepository.save(po));
    }


    public byte[] generatePdf(Integer id, String token) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));
        User user = userRepository
                .findById(jwtUtil.extractUserId(token))
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!(user.getDepartment() == Department.PROCUREMENT || isManagerOrAbove(user.getPosition())
                || user.getUserId().equals(po.getPurchaseRequisition().getRequestedBy().getUserId()))) {
            throw new RuntimeException("Unauthorized: Only procurement or manager+ can create POs");
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

                reqTable.addCell(PurchaseOrderUtils.getHeaderCell("Requisition ID"));
                reqTable.addCell(PurchaseOrderUtils.getHeaderCell("Requested By"));
                reqTable.addCell(PurchaseOrderUtils.getHeaderCell("Quantity"));

                reqTable.addCell(PurchaseOrderUtils.getBorderedCell(po.getPurchaseRequisition().getPrId().toString(), false));
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
            throw new RuntimeException("Error generating PDF: " + e.getMessage());
        }
    }



    private boolean isManagerOrAbove(Position position) {
        return switch (position) {
            case MANAGER, SENIOR_MANAGER, DIRECTOR, VICE_PRESIDENT, PRESIDENT, CXO, CEO -> true;
            default -> false;
        };
    }
}
