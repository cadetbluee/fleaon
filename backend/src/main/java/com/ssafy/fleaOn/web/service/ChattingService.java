package com.ssafy.fleaOn.web.service;

import com.ssafy.fleaOn.web.domain.Chatting;
import com.ssafy.fleaOn.web.domain.ChattingList;
import com.ssafy.fleaOn.web.domain.User;
import com.ssafy.fleaOn.web.domain.Trade;
import com.ssafy.fleaOn.web.dto.*;
import com.ssafy.fleaOn.web.repository.ChattingListRepository;
import com.ssafy.fleaOn.web.repository.ChattingRepository;
import com.ssafy.fleaOn.web.repository.TradeRepository;
import com.ssafy.fleaOn.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChattingService {

    private final ChattingRepository chattingRepository;
    private final ChattingListRepository chattingListRepository;
    private final UserRepository userRepository;
    private final TradeRepository tradeRepository;
    private final ChatBotService chatBotService;

    public List<ChattingResponse> getChattingByUserId(int userId) {
        List<Chatting> chatList = chattingRepository.findChattingByBuyerOrSellerWithView(userId);
        List<ChattingResponse> responses = new ArrayList<>();

        for (Chatting chat : chatList) {
            List<ChattingList> messages = chattingListRepository.findByChatting_ChattingId(chat.getChattingId())
                    .orElseThrow(() -> new RuntimeException("Chatting not found"));
            ChattingList recentMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);
            User otherUser;
            if (chat.getSeller().getUserId() == userId) {
                otherUser = userRepository.findByEmail(chat.getBuyer().getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("no user found for id: " + chat.getBuyer().getUserId()));
            } else {
                otherUser = userRepository.findByEmail(chat.getSeller().getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("no user found for id: " + chat.getSeller().getUserId()));
            }

            responses.add(new ChattingResponse(
                    chat,
                    otherUser.getNickname(), // Assuming User entity has a nickname field
                    otherUser.getProfilePicture(), // Assuming User entity has a profile field
                    recentMessage,
                    chat.getView(),  // 추가된 view 필드를 반영
                    chat.getLive().getLiveId(),
                    chat.getLive().getIsLive(),
                    chat.getBuyer().getUserId()==userId
            ));
        }
        // 리스트를 recentMessageTime에 따라 정렬
        responses.sort(Comparator.comparing(ChattingResponse::getRecentMessageTime).reversed());

        return responses;
    }

    public ChattingMessageResponse getChatMessage(int chattingId, User user) {
        Chatting chatting = chattingRepository.findById(chattingId)
                .orElseThrow(() -> new RuntimeException("Chatting not found"));
        List<ChattingList> chattingLists = chattingListRepository.findByChatting_ChattingId(chattingId)
                .orElseThrow(() -> new RuntimeException("No chatting messages found"));

        List<MessageResponse> messages = new ArrayList<>();
        for (ChattingList chattingList : chattingLists) {
            if(chatting.getSeller().equals(user) && !chattingList.isBot())
                continue;
            messages.add(new MessageResponse(chattingList));
        }

        User otherUser;
        if (chatting.getSeller().equals(user)) {
            otherUser = userRepository.findByEmail(chatting.getBuyer().getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            otherUser = userRepository.findByEmail(chatting.getSeller().getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        Trade trade = tradeRepository.findByChatting_chattingId(chatting.getChattingId())
                .orElseThrow(() -> new RuntimeException("Trade not found")).get(0);
        boolean isBuyer = trade.getBuyerId() == user.getUserId();

        return new ChattingMessageResponse(trade.getLive().getLiveId(), isBuyer, otherUser, messages);
    }

    public Chatting getChatByChattingId(int chattingId) {
        return chattingRepository.findById(chattingId)
                .orElseThrow(() -> new RuntimeException("Chatting not found"));
    }

    @Transactional  // 트랜잭션 추가
    public ChattingList createMessage(Chatting chatting, int writerId, ChattingMessageRequest chatContent) {
        System.out.println(LocalDateTime.now());

        // 직접 쿼리 실행
        chattingListRepository.saveWithoutTime(
                chatting.getChattingId(),
                writerId,
                chatContent.getContents(),
                chatContent.isBot()
        );

        // 보통 JPA에서는 insert나 update 시 바로 엔티티를 반환하지 않지만,
        // 필요하다면 쿼리 실행 후 해당 엔티티를 다시 조회할 수 있습니다.
        List<ChattingList> savedMessages = chattingListRepository.findByChatting_ChattingId(chatting.getChattingId()).orElseThrow(()->new RuntimeException("Chatting not found"));
        ChattingList savedMessage = savedMessages.get(savedMessages.size() - 1);
        if (!chatting.getView() && chatContent.isBot()) {
            chatBotService.startWithSellerChat(chatContent.getChattingId());
        }

        return savedMessage;
    }

}
