package com.ssafy.fleaOn.web.dto;

import com.ssafy.fleaOn.web.domain.Live;
import com.ssafy.fleaOn.web.domain.Product;
import com.ssafy.fleaOn.web.domain.Shorts;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultShortsResponse {

    private int shortsId;
    private String productName;
    private int productPrice;
    private String tradePlace;
    private LocalTime length;
    private String thumbnail;
    private boolean isScrap;
    private String dongName;

    public static ResultShortsResponse fromEntity(Shorts shorts, Product product, Live live) {
        return ResultShortsResponse.builder()
                .shortsId(shorts.getShortsId())
                .productName(product.getName())
                .productPrice(product.getPrice())
                .tradePlace(live.getTradePlace())
                .length(shorts.getLength())
                .thumbnail(shorts.getShortsThumbnail())
                .isScrap(false)
                .dongName(live.getRegionInfo().getEupmyeon())
                .build();
    }
}