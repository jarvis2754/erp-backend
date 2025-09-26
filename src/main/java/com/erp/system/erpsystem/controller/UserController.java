package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.user.UserDto;
import com.erp.system.erpsystem.dto.user.UserUpdateDto;
import com.erp.system.erpsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- Create User ---
    @PostMapping("/add")
    public ResponseEntity<UserDto> createUser(@RequestBody UserUpdateDto userUpdateDto) {
        UserDto created = userService.createUser(userUpdateDto);
        return ResponseEntity.ok(created);
    }

    // --- Update User ---
    @PatchMapping("/update/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Integer id,
                                              @RequestBody UserUpdateDto userUpdateDto) {
        UserDto updated = userService.updateUser(id, userUpdateDto);
        return ResponseEntity.ok(updated);
    }

    // --- Get User by UUID ---
    @GetMapping("/get/{uuid}")
    public ResponseEntity<UserDto> getUserByUUID(@PathVariable String uuid) {
        UserDto userDto = userService.getUserByUUID(uuid);
        return ResponseEntity.ok(userDto);
    }

    // --- Get All Users by OrgId ---
    @GetMapping("/org/{orgId}")
    public ResponseEntity<List<UserDto>> getAllUsersByOrg(@PathVariable Integer orgId) {
        List<UserDto> users = userService.getAllUsersByOrg(orgId);
        return ResponseEntity.ok(users);
    }

    // --- Delete User ---
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) return ResponseEntity.ok("User deleted successfully");
        return ResponseEntity.badRequest().body("User not found");
    }
}
