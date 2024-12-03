package com.mallangs.domain.chat.dto.response;

import com.mallangs.domain.chat.entity.MessageType;
import com.mallangs.domain.image.dto.ImageResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class ChatMessageListResponse {
    private Long chatMessageId;
    private Long chatRoomId;
    private String profileImage;
    private String chatMessageImage;
    private String sender;
    private String message;
    private MessageType type;
    private Boolean senderRead;
    private Boolean receiverRead;

    @Builder
    public ChatMessageListResponse(Long chatMessageId, Long chatRoomId,
                                   String profileImage, String chatMessageImage,
                                   String sender, String message, MessageType type,
                                   Boolean senderRead, Boolean receiverRead) {
        this.chatMessageId = chatMessageId;
        this.chatRoomId = chatRoomId;
        this.profileImage = profileImage;
        this.chatMessageImage = chatMessageImage;
        this.sender = sender;
        this.message = message;
        this.type = type;
        this.senderRead = senderRead;
        this.receiverRead = receiverRead;
    }
}