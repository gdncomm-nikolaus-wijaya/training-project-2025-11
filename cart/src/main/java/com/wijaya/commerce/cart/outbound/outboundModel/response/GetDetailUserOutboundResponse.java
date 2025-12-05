package com.wijaya.commerce.cart.outbound.outboundModel.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetDetailUserOutboundResponse {
    private String id;
    private String email;
    private String phoneNumber;
    private String name;
    private String status;
}
