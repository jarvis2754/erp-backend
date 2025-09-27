package com.erp.system.erpsystem.service;


import com.erp.system.erpsystem.dto.user.UserDto;
import com.erp.system.erpsystem.dto.user.UserUpdateDto;
import com.erp.system.erpsystem.model.Organization;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.repository.OrganizationRepository;
import com.erp.system.erpsystem.repository.UserRepository;
import com.erp.system.erpsystem.service.UserService;
import com.erp.system.erpsystem.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepo;
    private OrganizationRepository orgRepo;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepository.class);
        orgRepo = mock(OrganizationRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtUtil = mock(JwtUtil.class);
        userService = new UserService(userRepo, passwordEncoder, jwtUtil, orgRepo);
    }

    @Test
    void testCreateUser() {
        UserUpdateDto dto = new UserUpdateDto();

        dto.setUserName("John Doe");
        dto.setEmail("john@example.com");
        dto.setDepartment(Department.IT);
        dto.setPosition(Position.INTERN);
        dto.setPassword("pass123");
        dto.setOrgCode("ORG123");

        Organization org = new Organization();
        org.setOrgId(1);
        when(orgRepo.findByOrgCode("ORG123")).thenReturn(Optional.of(org));

        when(passwordEncoder.encode(any())).thenReturn("encodedPass");

        User savedUser = new User();
        savedUser.setUserId(1);
        savedUser.setUserName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setDepartment(Department.IT);
        savedUser.setPosition(Position.INTERN);
        savedUser.setPassword("encodedPass");
        savedUser.setOrganization(org);
        savedUser.setUuId("E001");

        when(userRepo.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals("John Doe", result.getUserName());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void testDeleteUserExists() {
        when(userRepo.existsById(1)).thenReturn(true);
        doNothing().when(userRepo).deleteById(1);

        boolean deleted = userService.deleteUser(1);
        assertTrue(deleted);

        verify(userRepo, times(1)).deleteById(1);
    }

    @Test
    void testDeleteUserNotExists() {
        when(userRepo.existsById(2)).thenReturn(false);
        boolean deleted = userService.deleteUser(2);
        assertFalse(deleted);

        verify(userRepo, never()).deleteById(2);
    }
}

