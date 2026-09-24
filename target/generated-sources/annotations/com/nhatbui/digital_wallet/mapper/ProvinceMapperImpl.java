package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.location.ProvinceResponse;
import com.nhatbui.digital_wallet.entity.Province;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-24T20:18:37+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class ProvinceMapperImpl implements ProvinceMapper {

    @Override
    public ProvinceResponse mapToProvinceResponse(Province province) {
        if ( province == null ) {
            return null;
        }

        ProvinceResponse.ProvinceResponseBuilder provinceResponse = ProvinceResponse.builder();

        provinceResponse.code( province.getCode() );
        provinceResponse.name( province.getName() );

        return provinceResponse.build();
    }
}
