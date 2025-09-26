package com.erp.system.erpsystem.service;

import com.erp.system.erpsystem.dto.user.UserDto;
import com.erp.system.erpsystem.dto.user.UserUpdateDto;
import com.erp.system.erpsystem.mapper.UserMapper;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // --- Create User ---
    @Transactional
    public UserDto createUser(UserUpdateDto userUpdateDto) {
        User user = new User();
        UserMapper.updateEntity(user, userUpdateDto);

        // encode password
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // generate UUID
        if (user.getUuId() == null) {
            user.setUuId(generateSequentialUuid(user));
        }

        // handle reporting manager via UUID
        if (userUpdateDto.getReportingManager() != null) {
            userRepo.findByUuId(userUpdateDto.getReportingManager())
                    .ifPresent(user::setReportingManager);
        }

        User saved = userRepo.save(user);
        return UserMapper.toDto(saved);
    }

    // --- Update User ---
    @Transactional
    public UserDto updateUser(Integer userId, UserUpdateDto userUpdateDto) {
        User existing = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserMapper.updateEntity(existing, userUpdateDto);

        if (userUpdateDto.getReportingManager() != null) {
            userRepo.findByUuId(userUpdateDto.getReportingManager())
                    .ifPresent(existing::setReportingManager);
        }

        if (userUpdateDto.getPassword() != null) {
            existing.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        }

        User updated = userRepo.save(existing);
        return UserMapper.toDto(updated);
    }

    // --- Get User by UUID ---
    public UserDto getUserByUUID(String uuid) {
        User user = userRepo.findByUuId(uuid)
                .orElseThrow(() -> new RuntimeException("User with UUID " + uuid + " not found"));
        return UserMapper.toDto(user);
    }

    // --- Get All Users by OrgId ---
    public List<UserDto> getAllUsersByOrg(Integer orgId) {
        return userRepo.findByOrganization_OrgId(orgId)
                .stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }


    // --- Delete User ---
    public boolean deleteUser(Integer userId) {
        if (userRepo.existsById(userId)) {
            userRepo.deleteById(userId);
            return true;
        }
        return false;
    }

    // --- Helper: UUID generator ---
    private String generateSequentialUuid(User user) {
        String prefix;

        switch (user.getPosition()) {
            case CEO -> { return "CEO"; }
            case CXO -> prefix = "CXO";
            case PRESIDENT -> prefix = "PRES";
            case DIRECTOR -> prefix = "DIR";
            case VICE_PRESIDENT -> prefix = "VP";
            case MANAGER, SENIOR_MANAGER -> prefix = "MGR";
            default -> prefix = (user.getDepartment() == Department.ADMINISTRATION) ? "ADM" : "E";
        }

        List<String> lastIds = userRepo.findTopByPrefix(prefix, PageRequest.of(0, 1));
        int nextNumber = 1;
        if (!lastIds.isEmpty()) {
            String lastId = lastIds.get(0);
            nextNumber = Integer.parseInt(lastId.replaceAll("\\D", "")) + 1;
        }
        return prefix + String.format("%03d", nextNumber);
    }
    public User findByEmailId(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    @Transactional
    public User registerUser(User user) {
        if (user.getUuId() == null) {
            user.setUuId(generateSequentialUuid(user));
        }
        if (user.getPassword() != null) {
            user.setPassword(user.getPassword());
        }
        return userRepo.save(user);
    }
}
