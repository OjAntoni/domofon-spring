package org.example.domofonbe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ChatConversationResponse {
    private long id;
    private List<UserResponse> participants;
    private List<MessageResponse> messages;
}
