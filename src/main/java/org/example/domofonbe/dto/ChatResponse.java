package org.example.domofonbe.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatResponse {
    private long id;
    private List<UserResponse> participants;
    private MessageResponse lastMessage;
    private long unreadCount;
}
