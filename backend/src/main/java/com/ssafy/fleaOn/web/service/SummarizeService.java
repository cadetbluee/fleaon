package com.ssafy.fleaOn.web.service;

import com.ssafy.fleaOn.web.domain.Shorts;
import com.ssafy.fleaOn.web.domain.ShortsContent;
import com.ssafy.fleaOn.web.domain.ShortsScrap;
import com.ssafy.fleaOn.web.dto.CategoryIdResponse;
import com.ssafy.fleaOn.web.dto.SummaryResponse;
import com.ssafy.fleaOn.web.repository.CategoryRepository;
import com.ssafy.fleaOn.web.repository.ShortsContentRepository;
import com.ssafy.fleaOn.web.repository.ShortsRepository;
import com.ssafy.fleaOn.web.repository.SummationRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SummarizeService {

    private final SummationRepository summationRepository;
    @Value("${OPENAI_API_KEY}")
    private String apiKey;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final ShortsContentRepository shortsContentRepository;

    private final ShortsRepository shortsRepository;

    public int summarize(String text, int shortsId) {
        String systemInstruction = "assistant는 user의 입력을 다음 형식으로 요약해준다:\n"
                + "COMMEND: 상품에 대한 칭찬(50자 내외로 요약)\n"
                + "DESCRIPTION: 사진에 대한 요약적인 설명(50자 내외로 요약)\n"
                + "PERIOD: 사용 기간\n"
                + "STATUS: 상품의 전체적인 상태(50자 내외로 요약)";

        // JSON 객체 생성
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");

        // messages 배열 구성
        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemInstruction);
        messages.put(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", text);
        messages.put(userMessage);

        requestBody.put("messages", messages);

        // RestTemplate 생성 및 요청
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject responseJson = new JSONObject(response.getBody());
                String summaryContent = responseJson.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                ShortsContent summaryResponse =  parseSummaryContent(summaryContent, shortsId);
                shortsContentRepository.save(summaryResponse);
                return shortsId;
            } else {
                // 에러 처리
                System.err.println("Error: " + response.getStatusCode() + " - " + response.getBody());
                Optional<Shorts> shorts = shortsRepository.findByShortsId(shortsId);
                ShortsContent summaryResult = ShortsContent.builder()
                        .shorts(shorts.get())
                        .commend("")
                        .description("")
                        .period("")
                        .status("")
                        .build();

                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private ShortsContent parseSummaryContent(String summaryContent, int shortsId) {
        String commend = extractSection(summaryContent, "COMMEND");
        String description = extractSection(summaryContent, "DESCRIPTION");
        String period = extractSection(summaryContent, "PERIOD");
        String status = extractSection(summaryContent, "STATUS");

        Optional<Shorts> optionalShorts = shortsRepository.findByShortsId(shortsId);

        if (!optionalShorts.isPresent()) {
            throw new IllegalArgumentException("Invalid shorts ID: " + shortsId);
        }

        ShortsContent summaryResult = ShortsContent.builder()
                .shorts(optionalShorts.get())
                .commend(commend)
                .description(description)
                .period(period)
                .status(status)
                .build();

        return summaryResult;
    }


    private String extractSection(String content, String section) {
        String sectionHeader = section + ":";
        int startIndex = content.indexOf(sectionHeader);
        if (startIndex == -1) return "";

        startIndex += sectionHeader.length();
        int endIndex = content.indexOf("\n", startIndex);
        if (endIndex == -1) endIndex = content.length();

        return content.substring(startIndex, endIndex).trim();
    }

    public SummaryResponse getSummary(int shortsId) {
        ShortsContent summation = summationRepository.findByShorts_ShortsId(shortsId).orElseThrow(()-> new IllegalArgumentException("쇼츠 요약본을 찾을 수 없습니다."));
        SummaryResponse response = SummaryResponse.builder()
                .shortId(shortsId)
                .commend(summation.getCommend())
                .description(summation.getDescription())
                .period(summation.getPeriod())
                .status(summation.getStatus())
                .build();
        return response;
    }
}