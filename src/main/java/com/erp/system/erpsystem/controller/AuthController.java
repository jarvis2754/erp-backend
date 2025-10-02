package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.auth.LoginRequestDTO;
import com.erp.system.erpsystem.dto.auth.SignUpOrganizationDto;
import com.erp.system.erpsystem.dto.user.UserDto;
import com.erp.system.erpsystem.mapper.UserMapper;
import com.erp.system.erpsystem.model.Organization;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.service.OrganizationService;
import com.erp.system.erpsystem.service.UserService;
import com.erp.system.erpsystem.utils.JwtUtil;
import com.erp.system.erpsystem.utils.OrganizationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

    @Autowired
    private OrganizationService orgService;

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody SignUpOrganizationDto userDTO) {
        try {

            if (userService.findByEmailId(userDTO.getUser().getEmail()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Email already in use"));
            }

            Organization organization = orgService.registerOrganization(OrganizationUtils.getOrganization(userDTO));

            // Register user
            User user = new User();
            user.setUserName(userDTO.getUser().getUserName());
            user.setEmail(userDTO.getUser().getEmail());
            user.setPassword(passwordEncoder.encode(userDTO.getUser().getPassword())); // encode once
            user.setPosition(Position.MANAGER);
            user.setDepartment(Department.ADMINISTRATION);
            user.setOrganization(organization);
            userService.registerUser(user);

            return ResponseEntity.ok(Map.of("message", "User and Organization registered successfully"));

        } catch (Exception e) {
            // Catch all exceptions and return message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Signup failed", "error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginDTO) {
        try {
            // Find user by email
            User user = userService.findByEmailId(loginDTO.getEmail());

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid email or password"));
            }

            // Check if the user belongs to the given organization
            if (!user.getOrganization().getOrgCode().equals(loginDTO.getOrgCode())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User does not belong to this organization"));
            }

            // Authenticate password
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
            );

            String token = jwtUtil.generateToken(user);

            UserDto userDto = UserMapper.toDto(user);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "token", token,
                    "user",userDto

            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid password", "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Login failed", "error", e.getMessage()));
        }
    }
}
