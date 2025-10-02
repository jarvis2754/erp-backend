package com.erp.system.erpsystem.utils;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;

import java.awt.Color;

/**
 * Utility class for creating styled PDF table cells specifically for Vendor Invoices.
 */
public class VendorInvoiceUtils {

    /**
     * Creates a table cell with optional bold text and borders.
     * Suitable for labels or normal data.
     */
    public static PdfPCell getBorderedCell(String text, boolean bold) {
        Font font = new Font(Font.HELVETICA, 12, bold ? Font.BOLD : Font.NORMAL, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(PdfPCell.BOX); // Border around cell
        return cell;
    }

    /**
     * Creates a header cell for tables with dark background and white bold text.
     * Suitable for table headers like "Item", "Amount", "Tax", etc.
     */
    public static PdfPCell getHeaderCell(String text) {
        Font font = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(50, 50, 50)); // dark gray
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(PdfPCell.BOX);
        return cell;
    }

    /**
     * Creates a table cell for numeric amounts, right-aligned.
     * Bold can be optionally applied.
     */
    public static PdfPCell getAmountCell(String text, boolean bold) {
        Font font = new Font(Font.HELVETICA, 12, bold ? Font.BOLD : Font.NORMAL, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(PdfPCell.BOX);
        return cell;
    }

    /**
     * Creates a note or footer cell (e.g., terms, remarks) with no border.
     */
    public static PdfPCell getNoteCell(String text) {
        Font font = new Font(Font.HELVETICA, 11, Font.ITALIC, Color.DARK_GRAY);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBorder(PdfPCell.NO_BORDER); // Correct for no border
        return cell;
    }

    /**
     * Creates a bold label cell, right-aligned (useful for invoice totals).
     */
    public static PdfPCell getLabelCell(String text) {
        Font font = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(PdfPCell.BOX);
        return cell;
    }
}
