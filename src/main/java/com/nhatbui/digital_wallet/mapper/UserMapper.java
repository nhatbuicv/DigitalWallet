package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.admin.UserAdminResponse;
import com.nhatbui.digital_wallet.dto.response.user.UserProfileResponse;
import com.nhatbui.digital_wallet.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring" , uses = ProvinceMapper.class)
public interface UserMapper {
    UserProfileResponse mapToUserProfileResponse(User user);

    @Mapping(target = "uid", expression = "java(user.getUid() != null ? user.getUid().toString() : null)")
    @Mapping(target = "citizenId", ignore = true)
    @Mapping(target = "totalWallets", ignore = true)
    @Mapping(target = "totalBalance", ignore = true)
    UserAdminResponse mapToUserAdminResponse(User user);
}