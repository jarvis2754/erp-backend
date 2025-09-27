package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.model.Permission;
import com.erp.system.erpsystem.model.enums.ActionType;
import com.erp.system.erpsystem.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    // 1️⃣ Get all permissions of a user
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserPermissions(@PathVariable Integer userId) {
        try {
            List<Permission> permissions = permissionService.getPermissionsByUser(userId);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching permissions: " + e.getMessage());
        }
    }

    // 2️⃣ Assign a permission
    @PostMapping("/assign/{userId}/{action}")
    public ResponseEntity<?> assignPermission(@PathVariable Integer userId, @PathVariable ActionType action) {
        try {
            Permission permission = permissionService.assignPermission(userId, action);
            return ResponseEntity.status(HttpStatus.CREATED).body(permission);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error assigning permission: " + e.getMessage());
        }
    }

    // 3️⃣ Revoke a permission
    @DeleteMapping("/revoke/{permissionId}")
    public ResponseEntity<?> revokePermission(@PathVariable Integer permissionId) {
        try {
            permissionService.revokePermission(permissionId);
            return ResponseEntity.ok("Permission revoked successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error revoking permission: " + e.getMessage());
        }
    }
}

