package com.wijaya.commerce.product.service.util;

import java.util.stream.Collectors;
import java.util.List;
import org.springframework.stereotype.Service;

import com.wijaya.commerce.product.commandImpl.Model.GetDetailProductCommandResponse;
import com.wijaya.commerce.product.restWebModel.response.GetDetailProductWebModel;
import com.wijaya.commerce.product.service.helper.CommonHelper;

@Service
public class ResponseHelper {

    public static GetDetailProductWebModel toGetDetailProductWebResponse(
            GetDetailProductCommandResponse commandResponse) {
        GetDetailProductWebModel webResponse = CommonHelper.copyProperties(commandResponse,
                new GetDetailProductWebModel());
        List<GetDetailProductWebModel.ProductImage> images = commandResponse.getImages().stream()
                .map(image -> GetDetailProductWebModel.ProductImage.builder()
                        .url(image.getUrl())
                        .alt(image.getAlt())
                        .isPrimary(image.getIsPrimary())
                        .build())
                .collect(Collectors.toList());
        webResponse.setImages(images);
        List<GetDetailProductWebModel.CategoryInfo> categories = commandResponse.getCategories().stream()
                .map(category -> GetDetailProductWebModel.CategoryInfo.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .collect(Collectors.toList());
        webResponse.setCategories(categories);

        return webResponse;
    }

}
