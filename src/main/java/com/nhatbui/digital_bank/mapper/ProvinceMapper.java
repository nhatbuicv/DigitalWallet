package com.nhatbui.digital_bank.mapper;

import com.nhatbui.digital_bank.dto.response.ProvinceResponse;
import com.nhatbui.digital_bank.entity.Province;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProvinceMapper {
    ProvinceResponse mapToProvinceResponse (Province province);
}
