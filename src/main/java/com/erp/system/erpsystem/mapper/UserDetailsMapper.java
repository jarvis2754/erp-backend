package com.erp.system.erpsystem.mapper;

import com.erp.system.erpsystem.dto.details.UserDetailsDto;
import com.erp.system.erpsystem.model.User;

public class UserDetailsMapper {
    public static UserDetailsDto toUserDetails(User user){
        return new UserDetailsDto(
                user.getUserId(),
                user.getUuId(),
                user.getUserName(),
                user.getEmail(),
                user.getDepartment(),
                user.getPosition()
        );
    }
}
