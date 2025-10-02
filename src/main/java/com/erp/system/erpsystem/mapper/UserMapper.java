package com.erp.system.erpsystem.mapper;

import com.erp.system.erpsystem.dto.details.UserDetailsDto;
import com.erp.system.erpsystem.dto.user.UserDto;
import com.erp.system.erpsystem.dto.user.UserUpdateDto;
import com.erp.system.erpsystem.model.User;

public class UserMapper {
    public static UserDto toDto(User user){
        UserDetailsDto reportingManager = (user.getReportingManager()!=null)? UserDetailsMapper.toUserDetails(user.getReportingManager()):null;
        String orgId = (user.getOrganization()!=null)? user.getOrganization().getOrgCode():null;
        return new UserDto(
                user.getUserName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getDepartment(),
                user.getPosition(),
                user.getStatus(),
                reportingManager,
                user.getUserId(),
                user.getUuId(),
                user.getJoiningDate(),
                orgId,
                user.getDateOfBirth(),
                user.getGender()
        );

    }

    public static void updateEntity(User user, UserUpdateDto dto) {
        if (dto.getUserName() != null) user.setUserName(dto.getUserName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getDepartment() != null) user.setDepartment(dto.getDepartment());
        if (dto.getPosition() != null) user.setPosition(dto.getPosition());
        if (dto.getPassword() != null) user.setPassword(dto.getPassword());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getDateOfBirth() != null) user.setDateOfBirth(dto.getDateOfBirth());
        // reportingManager handled in service using UUID
    }
}
