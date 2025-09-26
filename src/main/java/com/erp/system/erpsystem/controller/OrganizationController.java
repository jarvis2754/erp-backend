package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.organization.OrganizationRequestDto;
import com.erp.system.erpsystem.dto.organization.OrganizationResponseDto;
import com.erp.system.erpsystem.service.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/org")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    // Create Organization
    @PostMapping
    public ResponseEntity<?> createOrganization(@RequestBody OrganizationRequestDto dto) {
        try {
            return ResponseEntity.ok(organizationService.createOrganization(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // Get Organization by ID
    @GetMapping("/{orgId}")
    public ResponseEntity<?> getOrganization(@PathVariable Integer orgId) {
        try {
            return ResponseEntity.ok(organizationService.getOrganization(orgId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // Get All Organizations
    @GetMapping
    public ResponseEntity<List<OrganizationResponseDto>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    // Get Branches of an Organization
    @GetMapping("/{orgId}/branches")
    public ResponseEntity<?> getBranches(@PathVariable Integer orgId) {
        try {
            return ResponseEntity.ok(organizationService.getBranches(orgId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // Update Organization
    @PutMapping("/{orgId}")
    public ResponseEntity<?> updateOrganization(@PathVariable Integer orgId,
                                                @RequestBody OrganizationRequestDto dto) {
        try {
            return ResponseEntity.ok(organizationService.updateOrganization(orgId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // Delete Organization
    @DeleteMapping("/delete/{orgId}")
    public ResponseEntity<?> deleteOrganization(@PathVariable Integer orgId) {
        System.err.println(orgId);
        try {
            organizationService.deleteOrganization(orgId);
            return ResponseEntity.ok("Organization deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
