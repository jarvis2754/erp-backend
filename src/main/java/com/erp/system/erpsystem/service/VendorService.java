package com.erp.system.erpsystem.service;


import com.erp.system.erpsystem.dto.vendor.CreateVendorDto;
import com.erp.system.erpsystem.dto.vendor.VendorDto;
import com.erp.system.erpsystem.mapper.VendorMapper;
import com.erp.system.erpsystem.model.enums.Position;
import com.erp.system.erpsystem.model.procurement.Vendor;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.repository.UserRepository;
import com.erp.system.erpsystem.repository.VendorRepository;
import com.erp.system.erpsystem.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private User getCurrentUser(String token) {
        Integer userId = jwtUtil.extractUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void checkAccess(User user) {
        if (!(user.getDepartment() == Department.PROCUREMENT || isManagerOrAbove(user.getPosition()))) {
            throw new RuntimeException("Unauthorized: Only Procurement department can manage vendors");
        }
    }

    private boolean isManagerOrAbove(Position position) {
        return switch (position) {
            case MANAGER, SENIOR_MANAGER, DIRECTOR, VICE_PRESIDENT, PRESIDENT, CXO, CEO -> true;
            default -> false;
        };
    }

    public List<VendorDto> getAll(String token) {
        User user = getCurrentUser(token);
        checkAccess(user);

        return vendorRepository.findAll()
                .stream()
                .map(VendorMapper::toDto)
                .toList();
    }

    public VendorDto getById(Integer id, String token) {
        getCurrentUser(token); // access check
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        return VendorMapper.toDto(vendor);
    }

    public VendorDto create(CreateVendorDto dto, String token) {
        User user = getCurrentUser(token);
        checkAccess(user);

        Vendor vendor = VendorMapper.toEntity(dto);
        return VendorMapper.toDto(vendorRepository.save(vendor));
    }

    public VendorDto update(Integer id, CreateVendorDto dto, String token) {
        User user = getCurrentUser(token);
        checkAccess(user);

        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (dto.getName() != null) vendor.setName(dto.getName());
        if (dto.getEmail() != null) vendor.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) vendor.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getAddress() != null) vendor.setAddress(dto.getAddress());

        return VendorMapper.toDto(vendorRepository.save(vendor));
    }

    public void delete(Integer id, String token) {
        User user = getCurrentUser(token);
        checkAccess(user);

        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        vendorRepository.delete(vendor);
    }
}

