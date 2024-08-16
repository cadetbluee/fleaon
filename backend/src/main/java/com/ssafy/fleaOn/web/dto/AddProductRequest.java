package com.ssafy.fleaOn.web.dto;

import com.ssafy.fleaOn.web.domain.Live;
import com.ssafy.fleaOn.web.domain.Product;
import com.ssafy.fleaOn.web.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProductRequest {
    private String name;
    private int price;
    private int firstCategoryId;
    private int secondCategoryId;

    public Product toEntity(Live live, User seller) {
        return Product.builder()
                .live(live)
                .user(seller)
                .name(name)
                .price(price)
                .firstCategoryId(firstCategoryId)
                .secondCategoryId(secondCategoryId)
                .build();
    }
}
