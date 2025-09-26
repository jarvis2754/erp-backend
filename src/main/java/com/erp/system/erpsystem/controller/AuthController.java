package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.auth.LoginRequestDTO;
import com.erp.system.erpsystem.dto.auth.SignUpRequestDTO;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.service.UserService;
import com.erp.system.erpsystem.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody SignUpRequestDTO userDTO) {
        if (userService.findByEmailId(userDTO.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }

        User user = new User();
        user.setUserName(userDTO.getUserName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword())); // encode once
        user.setPosition(Position.MANAGER);
        user.setDepartment(Department.ADMINISTRATION);
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginDTO) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
            );
            System.out.println("1");

            User user = userService.findByEmailId(loginDTO.getEmail());
            String token = jwtUtil.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "token", token
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials","error",e.getMessage()));
        }
    }
}
