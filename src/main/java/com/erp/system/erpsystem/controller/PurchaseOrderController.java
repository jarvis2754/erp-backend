package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.purchaseorder.CreatePurchaseOrderDto;
import com.erp.system.erpsystem.dto.purchaseorder.PurchaseOrderDto;
import com.erp.system.erpsystem.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid Authorization header");
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String authHeader,@RequestParam(required = false) Integer orgId) {
        try {
            String token = extractToken(authHeader);
            List<PurchaseOrderDto> result = purchaseOrderService.getAllForUser(token,orgId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@RequestHeader("Authorization") String authHeader,@PathVariable Integer id) {
        try {
            String token = extractToken(authHeader);
            return ResponseEntity.ok(purchaseOrderService.getById(id,token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody CreatePurchaseOrderDto dto,
            @RequestHeader("Authorization") String authHeader

    ) {
        try {
            String token = extractToken(authHeader);
            PurchaseOrderDto result = purchaseOrderService.create(dto, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestParam String status,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = extractToken(authHeader);
            PurchaseOrderDto result = purchaseOrderService.updateStatus(id, status, token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> generatePdf(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        try {
            String token =authHeader.substring(7);
            byte[] pdf = purchaseOrderService.generatePdf(id,token);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=po-" + id + ".pdf")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

