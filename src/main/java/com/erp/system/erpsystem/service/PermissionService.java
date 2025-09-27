package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.model.Permission;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.ActionType;
import com.erp.system.erpsystem.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public List<ActionType> getUserActions(Integer userId) {
        return permissionRepository.findByUser_UserId(userId)
                .stream()
                .map(Permission::getAction)
                .collect(Collectors.toList());
    }

    public boolean hasPermission(Integer userId, ActionType action) {
        return getUserActions(userId).contains(action);
    }

    public Permission assignPermission(Integer userId, ActionType action) {
        Permission permission = new Permission();
        User user = new User();
        user.setUserId(userId);
        permission.setUser(user);
        permission.setAction(action);
        return permissionRepository.save(permission);
    }

    public void revokePermission(Integer permissionId) {
        permissionRepository.deleteById(permissionId);
    }

    public List<Permission> getPermissionsByUser(Integer userId) {
        return permissionRepository.findByUser_UserId(userId);
    }
}
