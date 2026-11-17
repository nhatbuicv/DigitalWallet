package com.nhatbui.wallet_ledger.mapper;

import com.nhatbui.wallet_ledger.dto.response.admin.UserAdminResponse;
import com.nhatbui.wallet_ledger.dto.response.user.UserProfileResponse;
import com.nhatbui.wallet_ledger.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = ProvinceMapper.class)
public interface UserMapper {
    @Mapping(target = "provinceResponse", source = "province")
    UserProfileResponse mapToUserProfileResponse(User user);

    UserAdminResponse mapToUserAdminResponse(User user);
}
