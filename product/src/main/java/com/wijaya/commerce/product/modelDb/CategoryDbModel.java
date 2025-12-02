package com.wijaya.commerce.product.modelDb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.wijaya.commerce.product.constant.CollectionProductName;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = CollectionProductName.CATEGORY)
public class CategoryDbModel {

    @Id
    private String id;
    private String name;
    private String description;
    private String image;
    private Integer sortOrder;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
