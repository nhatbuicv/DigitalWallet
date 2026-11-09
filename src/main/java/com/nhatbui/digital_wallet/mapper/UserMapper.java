package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.admin.UserAdminResponse;
import com.nhatbui.digital_wallet.dto.response.user.UserProfileResponse;
import com.nhatbui.digital_wallet.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = ProvinceMapper.class)
public interface UserMapper {
    @Mapping(target = "provinceResponse", source = "province")
    UserProfileResponse mapToUserProfileResponse(User user);

    UserAdminResponse mapToUserAdminResponse(User user);
}
