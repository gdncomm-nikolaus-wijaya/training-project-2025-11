package com.wijaya.commerce.cart.outbound.outboundService;

import com.wijaya.commerce.cart.outbound.outboundModel.response.GetDetailProductOutboundResponse;
import com.wijaya.commerce.cart.outbound.outboundModel.response.WebResponse;

public interface ProductOutboundService {
    WebResponse<?> getProductDetail(String sku);

}
