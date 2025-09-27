package com.erp.system.erpsystem.dto.vendor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVendorDto {
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
}

