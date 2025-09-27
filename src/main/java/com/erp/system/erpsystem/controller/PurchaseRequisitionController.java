package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.purchaserequisition.CreatePurchaseRequisitionDto;
import com.erp.system.erpsystem.dto.purchaserequisition.PurchaseRequisitionDto;
import com.erp.system.erpsystem.dto.purchaserequisition.UpdatePRStatusDto;
import com.erp.system.erpsystem.service.PurchaseRequisitionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/requisition")
public class PurchaseRequisitionController {

    private final PurchaseRequisitionService prService;

    public PurchaseRequisitionController(PurchaseRequisitionService prService) {
        this.prService = prService;
    }

    @GetMapping
    public ResponseEntity<?> getAllPRs(@RequestHeader("Authorization") String authHeader,
                                       @RequestParam(required = false) Integer orgId) {
        String token = authHeader.substring(7);
        try {
            List<PurchaseRequisitionDto> prs = prService.getAllPRs(orgId, token);
            return ResponseEntity.ok(prs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPRById(@RequestHeader("Authorization") String authHeader,@PathVariable Integer id) {
        String token = authHeader.substring(7);
        try {
            PurchaseRequisitionDto pr = prService.getPRById(token,id);
            return ResponseEntity.ok(pr);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createPR(@RequestHeader("Authorization") String authHeader, @RequestBody CreatePurchaseRequisitionDto dto) {
        String token = authHeader.substring(7);
        try {
            PurchaseRequisitionDto pr = prService.createPR(token, dto);
            return ResponseEntity.ok(pr);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable Integer id,
                                          @RequestBody UpdatePRStatusDto dto) {
        String token = authHeader.substring(7);
        try {
            PurchaseRequisitionDto pr = prService.updateStatus(id, dto, token);
            return ResponseEntity.ok(pr);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // Simple runtime exception handler
    private ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("message", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
