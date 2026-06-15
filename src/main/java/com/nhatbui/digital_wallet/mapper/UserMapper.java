package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.user.UserProfileResponse;
import com.nhatbui.digital_wallet.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring" , uses = ProvinceMapper.class)
public interface UserMapper {
    UserProfileResponse mapToUserProfileResponse(User user);
}
