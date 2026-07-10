package com.nhatbui.digital_wallet.dto.response.location;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProvinceResponse {
    private String code;
    private String name;
}