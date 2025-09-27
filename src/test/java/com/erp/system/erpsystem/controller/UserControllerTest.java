package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.user.UserDto;
import com.erp.system.erpsystem.dto.user.UserUpdateDto;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService; // mock service layer

    // --- Test Create User ---
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateUser() throws Exception {
        UserUpdateDto request = new UserUpdateDto();
        request.setUserName("John Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("1234567890");
        request.setDepartment(Department.IT);
        request.setPosition(Position.INTERN);
        request.setPassword("pass123");
        request.setOrgCode("ORG123");

        UserDto response = new UserDto();
        response.setUserName("John Doe");
        response.setEmail("john@example.com");
        response.setPhoneNumber("1234567890");
        response.setDepartment(Department.IT);
        response.setPosition(Position.INTERN);
        response.setUserId(1);
        response.setUuId("uuid123");
        response.setOrgId("ORG123");

        Mockito.when(userService.createUser(any(UserUpdateDto.class))).thenReturn(response);

        mockMvc.perform(post("/users/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    // --- Test Get User by UUID ---
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetUserByUUID() throws Exception {
        UserDto response = new UserDto();
        response.setUserName("John Doe");
        response.setEmail("john@example.com");
        response.setUserId(1);
        response.setUuId("uuid123");

        Mockito.when(userService.getUserByUUID(anyString())).thenReturn(response);

        mockMvc.perform(get("/users/get/uuid123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("John Doe"))
                .andExpect(jsonPath("$.uuId").value("uuid123"));
    }

    // --- Test Delete User ---
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteUser() throws Exception {
        Mockito.when(userService.deleteUser(anyInt())).thenReturn(true);

        mockMvc.perform(delete("/users/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    // --- Test Get All Users by Org ---
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetAllUsersByOrg() throws Exception {
        UserDto user = new UserDto();
        user.setUserName("John Doe");
        user.setEmail("john@example.com");
        user.setUserId(1);
        user.setUuId("uuid123");

        Mockito.when(userService.getAllUsersByOrg(anyInt())).thenReturn(List.of(user));

        mockMvc.perform(get("/users/org/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("John Doe"));
    }
}
