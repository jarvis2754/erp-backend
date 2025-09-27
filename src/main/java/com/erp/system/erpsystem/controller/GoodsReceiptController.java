package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.goodsreceipt.CreateGoodsReceiptDto;
import com.erp.system.erpsystem.dto.goodsreceipt.GoodsReceiptDto;
import com.erp.system.erpsystem.service.GoodsReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goods-receipts")
@RequiredArgsConstructor
public class GoodsReceiptController {

    private final GoodsReceiptService grService;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            List<GoodsReceiptDto> grList = grService.getAll(token);
            return ResponseEntity.ok(grList);
        } catch (RuntimeException ex) {
            return errorResponse(ex);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        try {
            String token = authHeader.substring(7);
            GoodsReceiptDto gr = grService.getById(id, token);
            return ResponseEntity.ok(gr);
        } catch (RuntimeException ex) {
            return errorResponse(ex);
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("Authorization") String authHeader, @RequestBody CreateGoodsReceiptDto dto) {
        try {
            String token = authHeader.substring(7);
            GoodsReceiptDto gr = grService.create(dto, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(gr);
        } catch (RuntimeException ex) {
            return errorResponse(ex);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id, @RequestBody CreateGoodsReceiptDto dto) {
        try {
            String token = authHeader.substring(7);
            GoodsReceiptDto gr = grService.update(id, dto, token);
            return ResponseEntity.ok(gr);
        } catch (RuntimeException ex) {
            return errorResponse(ex);
        }
    }

    private ResponseEntity<Map<String, String>> errorResponse(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
