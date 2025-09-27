package com.erp.system.erpsystem.mapper;

import com.erp.system.erpsystem.dto.vendor.CreateVendorDto;
import com.erp.system.erpsystem.dto.vendor.VendorDto;
import com.erp.system.erpsystem.model.procurement.Vendor;

public class VendorMapper {

    public static VendorDto toDto(Vendor vendor) {
        return VendorDto.builder()
                .vendorId(vendor.getVendorId())
                .name(vendor.getName())
                .email(vendor.getEmail())
                .phoneNumber(vendor.getPhoneNumber())
                .address(vendor.getAddress())
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt())
                .build();
    }

    public static Vendor toEntity(CreateVendorDto dto) {
        return Vendor.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .build();
    }
}
