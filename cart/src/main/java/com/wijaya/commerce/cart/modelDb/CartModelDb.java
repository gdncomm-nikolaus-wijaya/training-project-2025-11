package com.wijaya.commerce.cart.modelDb;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.wijaya.commerce.cart.constant.CollectionCart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = CollectionCart.COLLECTION_CART)
public class CartModelDb {

    @Id
    private ObjectId id;

    private String userId;

    private List<CartItem> items;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItem {

        private String productSku;

        private String productName;

        private Long price;

        private Long comparePrice;

        private Integer discountPercentage;

        private Integer quantity;

        private Integer subTotal;

        private LocalDateTime addedAt;
    }
}
