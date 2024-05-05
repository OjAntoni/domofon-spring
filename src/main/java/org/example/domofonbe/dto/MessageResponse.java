package org.example.domofonbe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {
    private long id;
    private String text;
    private String from;
    private LocalDateTime dateTime;
    private String status;
    public MessageResponse(String text) {
        this.text = text;
    }

    public MessageResponse(String text, String from, LocalDateTime dateTime) {
        this.text = text;
        this.from = from;
        this.dateTime = dateTime;
    }
}
