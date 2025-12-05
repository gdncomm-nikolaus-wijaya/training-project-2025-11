package com.wijaya.commerce.cart.outbond.outbondService;

import com.wijaya.commerce.cart.outbond.outbondModel.response.GetDetailProductOutbondResponse;

public interface ProductOutbondService {
    GetDetailProductOutbondResponse getProductDetail(String sku);

}
