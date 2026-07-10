package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.location.ProvinceResponse;
import com.nhatbui.digital_wallet.entity.Province;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProvinceMapper {
    ProvinceResponse mapToProvinceResponse (Province province);
}
