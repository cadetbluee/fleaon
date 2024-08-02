package com.ssafy.fleaOn.web.dto;

import com.ssafy.fleaOn.web.domain.Live;
import com.ssafy.fleaOn.web.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddLiveRequest {
    private int liveId;
    private String title;
    private String liveDate; // LocalDateTime 대신 String 사용
    private String liveThumbnail;
    private String tradePlace;
    private List<AddProductRequest> product;

    public Live toEntity(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime parsedDate = LocalDateTime.parse(liveDate, formatter);
        return Live.builder()
                .title(title)
                .liveDate(parsedDate)
                .liveThumbnail(liveThumbnail)
                .tradePlace(tradePlace)
                .seller(user)
                .build();
    }
}
