package com.wijaya.commerce.cart.outbond.outbondModel.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetDetailUserOutbondResponse {
    private String id;
    private String email;
    private String phoneNumber;
    private String name;
    private String status;
}
