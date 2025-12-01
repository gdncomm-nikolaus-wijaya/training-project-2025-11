package com.wijaya.commerce.product.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.wijaya.commerce.product.command.CommandExecutor;
import com.wijaya.commerce.product.command.GetDetailProductCommand;
import com.wijaya.commerce.product.commandImpl.Model.GetDetailProductCommandRequest;
import com.wijaya.commerce.product.commandImpl.Model.GetDetailProductCommandResponse;
import com.wijaya.commerce.product.constant.ProductApiPath;
import com.wijaya.commerce.product.restWebModel.response.GetDetailProductWebModel;
import com.wijaya.commerce.product.restWebModel.response.WebResponse;
import com.wijaya.commerce.product.service.util.ResponseHelper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProductController {
        private final CommandExecutor commandExecutor;

        @GetMapping(ProductApiPath.PRODUCT + "/{sku}")
        public WebResponse<GetDetailProductWebModel> getProduct(@PathVariable String sku) {
                GetDetailProductCommandRequest commandRequest = GetDetailProductCommandRequest.builder()
                                .sku(sku)
                                .build();

                GetDetailProductCommandResponse commandResponse = commandExecutor.execute(GetDetailProductCommand.class,
                                commandRequest);

                return WebResponse.<GetDetailProductWebModel>builder()
                                .success(true)
                                .data(ResponseHelper.toGetDetailProductWebResponse(commandResponse))
                                .build();
        }
}
