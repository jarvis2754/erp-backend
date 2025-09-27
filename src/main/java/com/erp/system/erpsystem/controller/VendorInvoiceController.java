package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.vendorinvoice.CreateVendorInvoiceDto;
import com.erp.system.erpsystem.dto.vendorinvoice.VendorInvoiceDto;
import com.erp.system.erpsystem.service.VendorInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vendor-invoices")
@RequiredArgsConstructor
public class VendorInvoiceController {

    private final VendorInvoiceService viService;

    // GET all invoices
    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            List<VendorInvoiceDto> invoices = viService.getAll(token);
            return ResponseEntity.ok(invoices);
        } catch (RuntimeException ex) {
            return errorResponse(ex);
        }
    }

    // GET invoice by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@RequestHeader("Authorization") String authHeader,
                                     @PathVariable Integer id) {
        try {
            String token = authHeader.substring(7);
            VendorInvoiceDto invoice = viService.getById(token, id);
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException ex) {
            return errorResponse(ex);
        }
    }

    // POST create new invoice
    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("Authorization") String authHeader,
                                    @RequestBody CreateVendorInvoiceDto dto) {
        try {
            String token = authHeader.substring(7);
            VendorInvoiceDto invoice = viService.create(token, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
        } catch (RuntimeException ex) {
            return errorResponse(ex);
        }
    }

    // PATCH update invoice status
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateStatus(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable Integer id,
                                          @RequestParam String status) {
        try {
            String token = authHeader.substring(7);
            VendorInvoiceDto invoice = viService.updateStatus(token, id, status);
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException ex) {
            return errorResponse(ex);
        }
    }

    // Basic runtime exception handler
    private ResponseEntity<Map<String, String>> errorResponse(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}

