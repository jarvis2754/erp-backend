package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.user.UserDto;
import com.erp.system.erpsystem.dto.user.UserUpdateDto;
import com.erp.system.erpsystem.model.Organization;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.repository.OrganizationRepository;
import com.erp.system.erpsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrganizationRepository orgRepo;

    @Test
    void testCreateAndFetchUser() {
        // create organization first
        Organization org = new Organization();
        org.setOrgCode("ORG123");
        orgRepo.save(org);

        UserUpdateDto dto = new UserUpdateDto();
        dto.setUserName("Jane Doe");
        dto.setEmail("jane@example.com");
        dto.setDepartment(Department.HR);
        dto.setPosition(Position.ASSOCIATE);
        dto.setOrgCode("ORG123");

        UserDto created = userService.createUser(dto);
        assertNotNull(created.getUuId());

        UserDto fetched = userService.getUserByUUID(created.getUuId());
        assertEquals("Jane Doe", fetched.getUserName());
    }
}

