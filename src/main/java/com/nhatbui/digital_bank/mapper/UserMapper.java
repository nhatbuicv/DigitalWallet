package com.nhatbui.digital_bank.mapper;

import com.nhatbui.digital_bank.dto.response.user.UserProfileResponse;
import com.nhatbui.digital_bank.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring" , uses = ProvinceMapper.class)
public interface UserMapper {
    UserProfileResponse mapToUserProfileResponse(User user);
}
