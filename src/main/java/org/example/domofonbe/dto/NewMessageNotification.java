package org.example.domofonbe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewMessageNotification {
    private long id;
    private long chatId;
    private String text;
    private String from;
    private LocalDateTime dateTime;
    private String status;
}
