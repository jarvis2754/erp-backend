package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.vendor.CreateVendorDto;
import com.erp.system.erpsystem.dto.vendor.VendorDto;
import com.erp.system.erpsystem.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid Authorization header");
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            List<VendorDto> result = vendorService.getAll(token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            VendorDto vendor = vendorService.getById(id, token);
            return ResponseEntity.ok(vendor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateVendorDto dto, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            VendorDto vendor = vendorService.create(dto, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(vendor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody CreateVendorDto dto, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            VendorDto vendor = vendorService.update(id, dto, token);
            return ResponseEntity.ok(vendor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            vendorService.delete(id, token);
            return ResponseEntity.ok("Vendor deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

