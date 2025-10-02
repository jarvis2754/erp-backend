package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.permission.CreatePermissionRequestDto;
import com.erp.system.erpsystem.dto.permission.PermissionRequestDto;
import com.erp.system.erpsystem.dto.permission.UpdatePermissionStatusDto;
import com.erp.system.erpsystem.service.PermissionRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PermissionRequestController {

    private final PermissionRequestService service;

    @Autowired
    public PermissionRequestController(PermissionRequestService service) {
        this.service = service;
    }

    @PostMapping("/orgs/permissions")
    public ResponseEntity<?> createPermission(@RequestHeader("Authorization") String authHeader,
                                              @Valid @RequestBody CreatePermissionRequestDto dto) {
        String token = authHeader.substring(7);
        try {
            PermissionRequestDto created = service.create(dto, token);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/orgs/{orgId}/permissions")
    public ResponseEntity<Page<PermissionRequestDto>> listByOrg(@PathVariable String orgId,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size,
                                                                @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PermissionRequestDto> result = service.listByOrganization(orgId, pageable, status);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/permissions/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<?> listByUser(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(service.listByUser(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users/permissions")
    public ResponseEntity<?> listByCurrentUser(@RequestHeader("Authorization") String authHeader) {

        try {
            String token  = authHeader.substring(7);
            return ResponseEntity.ok(service.listByCurrentUser(token));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/permissions/{id}")
    public ResponseEntity<?> update(@RequestHeader("Authorization") String authHeader,
                                    @PathVariable Integer id,
                                    @Valid @RequestBody CreatePermissionRequestDto dto) {
        String token = authHeader.substring(7);
        try {
            return ResponseEntity.ok(service.update(token, id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/permissions/{id}/status")
    public ResponseEntity<?> changeStatus(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable Integer id,
                                          @Valid @RequestBody UpdatePermissionStatusDto dto) {
        String token = authHeader.substring(7);
        try {
            return ResponseEntity.ok(service.changeStatus(token, id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/permissions/pending")
    public ResponseEntity<List<PermissionRequestDto>> pendingApprovals(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(service.pendingApprovals(token));
    }

    @DeleteMapping("/permissions/{id}")
    public ResponseEntity<?> delete(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        String token = authHeader.substring(7);
        try {
            service.delete(token, id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
