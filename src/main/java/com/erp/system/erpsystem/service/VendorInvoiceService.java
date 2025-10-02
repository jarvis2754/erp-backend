package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.vendorinvoice.CreateVendorInvoiceDto;
import com.erp.system.erpsystem.dto.vendorinvoice.PdfVendorInvoiceDto;
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
import com.erp.system.erpsystem.utils.VendorInvoiceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.lowagie.text.*;
import com.lowagie.text.Font;

import com.lowagie.text.pdf.PdfPTable;

import com.lowagie.text.pdf.PdfWriter;

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

    private boolean isManagerOrAbove(Position pos) {
        return pos.ordinal() >= Position.MANAGER.ordinal();
    }

    private boolean isTopManagement(User user) {
        Position pos = user.getPosition();
        return pos == Position.CXO || pos == Position.CEO;
    }

    // --- Create Vendor Invoice ---
    private boolean canCreateInvoice(User user) {
        Department dept = user.getDepartment();
        Position pos = user.getPosition();
        return (dept == Department.PROCUREMENT || dept == Department.FINANCE)
                && pos.ordinal() >= Position.EXECUTIVE.ordinal();
    }

    // --- View Vendor Invoice ---
    private boolean canViewInvoice(User user) {
        Department dept = user.getDepartment();
        Position pos = user.getPosition();
        return dept == Department.PROCUREMENT
                || dept == Department.FINANCE
                || (dept == Department.ADMINISTRATION || (dept == Department.OPERATIONS) && isManagerOrAbove(pos))
                || isTopManagement(user);
    }

    // --- Approve / Update Status Vendor Invoice ---
    private boolean canApproveInvoice(User user) {
        Department dept = user.getDepartment();
        Position pos = user.getPosition();
        return ((dept == Department.PROCUREMENT || dept == Department.FINANCE) && isManagerOrAbove(pos))
                || isTopManagement(user);
    }

    // --- Generate PDF Access ---
    private boolean canGenerateInvoicePdf(User user) {
        return canCreateInvoice(user) || canViewInvoice(user);
    }

    // --- Create Invoice ---
    public VendorInvoiceDto create(String token, CreateVendorInvoiceDto dto) {
        User user = getUserFromToken(token);
        if (!canCreateInvoice(user)) {
            throw new RuntimeException("Unauthorized: Only Procurement/Finance Exec+ can create invoices");
        }

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

    // --- Get all invoices ---
    public List<VendorInvoiceDto> getAll(String token) {
        User user = getUserFromToken(token);
        if (!canViewInvoice(user)) {
            throw new RuntimeException("Unauthorized: Cannot view invoices");
        }

        Integer orgId = getOrgIdFromToken(token);
        return viRepository.findByPurchaseOrder_Org_OrgId(orgId)
                .stream()
                .map(VendorInvoiceMapper::toDto)
                .collect(Collectors.toList());
    }

    // --- Get invoice by ID ---
    public VendorInvoiceDto getById(String token, Integer id) {
        User user = getUserFromToken(token);
        if (!canViewInvoice(user)) {
            throw new RuntimeException("Unauthorized: Cannot view invoice");
        }

        VendorInvoice vi = viRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor Invoice not found"));
        return VendorInvoiceMapper.toDto(vi);
    }

    // --- Update status (Approve) ---
    public VendorInvoiceDto updateStatus(String token, Integer id, String status) {
        User user = getUserFromToken(token);
        if (!canApproveInvoice(user)) {
            throw new RuntimeException("Unauthorized: Only Procurement/Finance Manager+ or Top Management can approve/update invoice");
        }

        VendorInvoice vi = viRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor Invoice not found"));
        vi.setStatus(Enum.valueOf(InvoiceStatus.class, status.toUpperCase()));
        return VendorInvoiceMapper.toDto(viRepository.save(vi));
    }

    // --- Generate Vendor Invoice PDF ---
    public byte[] generateVendorInvoicePdf(Integer id, String token) {
        VendorInvoice vi = viRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor Invoice not found"));
        User user = getUserFromToken(token);

        if (!canGenerateInvoicePdf(user)) {
            throw new RuntimeException("Unauthorized: You do not have permission to generate Vendor Invoice PDF");
        }
        PdfVendorInvoiceDto invoice = VendorInvoiceMapper.toPdfDto(vi);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // --- Title ---
            Paragraph title = new Paragraph("Vendor Invoice", new Font(Font.HELVETICA, 20, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // --- Vendor Info ---
            Paragraph vendorName = new Paragraph(invoice.getVendorName(), new Font(Font.HELVETICA, 16, Font.BOLD));
            vendorName.setAlignment(Element.ALIGN_CENTER);
            document.add(vendorName);

            if (invoice.getVendorContact() != null) {
                Paragraph vendorContact = new Paragraph("Contact: " + invoice.getVendorContact());
                vendorContact.setAlignment(Element.ALIGN_CENTER);
                document.add(vendorContact);
            }

            if (invoice.getVendorAddress() != null) {
                Paragraph vendorAddress = new Paragraph("Address: " + invoice.getVendorAddress());
                vendorAddress.setAlignment(Element.ALIGN_CENTER);
                document.add(vendorAddress);
            }

            document.add(new Paragraph(" ")); // empty line

            // --- Invoice & Reference Info Table ---
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(20);
            infoTable.setSpacingAfter(20);
            infoTable.setWidths(new int[]{1, 2});

            infoTable.addCell(VendorInvoiceUtils.getBorderedCell("Invoice ID:", true));
            infoTable.addCell(VendorInvoiceUtils.getBorderedCell(invoice.getInvoiceId().toString(), false));

            infoTable.addCell(VendorInvoiceUtils.getBorderedCell("PO ID:", true));
            infoTable.addCell(VendorInvoiceUtils.getBorderedCell(invoice.getPoId().toString(), false));

            infoTable.addCell(VendorInvoiceUtils.getBorderedCell("GRN ID:", true));
            infoTable.addCell(VendorInvoiceUtils.getBorderedCell(invoice.getGrnId().toString(), false));

            infoTable.addCell(VendorInvoiceUtils.getBorderedCell("Invoice Date:", true));
            infoTable.addCell(VendorInvoiceUtils.getBorderedCell(invoice.getInvoiceDate().toString(), false));

            infoTable.addCell(VendorInvoiceUtils.getBorderedCell("Status:", true));
            infoTable.addCell(VendorInvoiceUtils.getBorderedCell(invoice.getStatus().toString(), false));

            document.add(infoTable);

            // --- Amount & Tax Table ---
            PdfPTable amountTable = new PdfPTable(2);
            amountTable.setWidthPercentage(40);
            amountTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            amountTable.setSpacingBefore(20);

            amountTable.addCell(VendorInvoiceUtils.getLabelCell("Invoice Amount:"));
            amountTable.addCell(VendorInvoiceUtils.getAmountCell(invoice.getInvoiceAmount().toString(), false));

            amountTable.addCell(VendorInvoiceUtils.getLabelCell("Taxes / GST:"));
            amountTable.addCell(VendorInvoiceUtils.getAmountCell(invoice.getTaxDetails(), false));

            document.add(amountTable);

            // --- Footer: Created & Updated ---
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setSpacingBefore(30);
            footerTable.setWidths(new int[]{1, 2});

            footerTable.addCell(VendorInvoiceUtils.getBorderedCell("Created At:", true));
            footerTable.addCell(VendorInvoiceUtils.getBorderedCell(invoice.getCreatedAt().toString(), false));

            footerTable.addCell(VendorInvoiceUtils.getBorderedCell("Updated At:", true));
            footerTable.addCell(VendorInvoiceUtils.getBorderedCell(invoice.getUpdatedAt().toString(), false));

            document.add(footerTable);

            // --- Notes / Terms ---
            PdfPTable noteTable = new PdfPTable(1);
            noteTable.setWidthPercentage(100);
            noteTable.setSpacingBefore(20);
            noteTable.addCell(VendorInvoiceUtils.getNoteCell("Thank you for your business!"));
            document.add(noteTable);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating Vendor Invoice PDF: " + e.getMessage(), e);
        }
    }


}

