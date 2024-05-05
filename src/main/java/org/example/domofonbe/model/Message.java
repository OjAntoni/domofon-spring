package org.example.domofonbe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    @ManyToOne
    private User author;
    private LocalDateTime dateTime;
    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    public Message(String content, User author) {
        this.content = content;
        this.author = author;
    }

    public Message(String content, User author, MessageStatus status) {
        this.content = content;
        this.author = author;
        this.status = status;
    }

    public Message(Long id, String content, User author,  MessageStatus status) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.status = status;
    }


    @PrePersist
    private void preSave(){
        dateTime = LocalDateTime.now();
    }
}

