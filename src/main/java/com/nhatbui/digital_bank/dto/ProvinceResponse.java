package com.nhatbui.digital_bank.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProvinceResponse {
    private String code;
    private String name;
}