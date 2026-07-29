package com.nhatbui.digital_wallet.dto.response.location;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProvinceResponse {
    private String code;
    private String name;
}