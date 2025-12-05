package com.wijaya.commerce.cart.outbound.outboundService;

import com.wijaya.commerce.cart.outbound.outboundModel.response.GetDetailUserOutboundResponse;
import com.wijaya.commerce.cart.outbound.outboundModel.response.WebResponse;

public interface UserOutboundService {
    WebResponse<?> getUserDetail(String id);
}
