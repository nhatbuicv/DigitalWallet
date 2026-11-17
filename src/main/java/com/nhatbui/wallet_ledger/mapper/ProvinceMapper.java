package com.nhatbui.wallet_ledger.mapper;

import com.nhatbui.wallet_ledger.dto.response.location.ProvinceResponse;
import com.nhatbui.wallet_ledger.entity.Province;
import org.mapstruct.Mapper;

@Mapper
public interface ProvinceMapper {
    ProvinceResponse mapToProvinceResponse(Province province);
}
