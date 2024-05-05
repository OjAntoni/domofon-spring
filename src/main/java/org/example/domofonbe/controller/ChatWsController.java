package org.example.domofonbe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.domofonbe.dto.MessageResponse;
import org.example.domofonbe.dto.NewMessageNotification;
import org.example.domofonbe.model.Chat;
import org.example.domofonbe.model.Message;
import org.example.domofonbe.model.MessageStatus;
import org.example.domofonbe.model.User;
import org.example.domofonbe.repository.ChatRepository;
import org.example.domofonbe.repository.MessageRepository;
import org.example.domofonbe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Controller
public class ChatWsController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate; //its for send to dynamical url destinations
    @Autowired
    private ObjectMapper json;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRepository chatRepository;

    @MessageMapping("/chat/send/{id}")
    @SendTo("/topic/messages/{id}")
    @Transactional
    public MessageResponse send(@DestinationVariable long id, MessageResponse message) throws Exception {

        long msgRespId = message.getId();
        Optional<Message> byId = messageRepository.findById(msgRespId);
        MessageResponse resp;

        if(byId.isPresent()){
            log.info("Message is present, updating it...");
            Message msg = byId.get();
            Message save = messageRepository.save(Message.builder().id(msgRespId).content(message.getText()).author(msg.getAuthor()).status(MessageStatus.valueOf(message.getStatus())).dateTime(msg.getDateTime()).build());
            resp = MessageResponse.builder().id(save.getId()).status(message.getStatus()).from(save.getAuthor().getUsername()).text(save.getContent()).dateTime(save.getDateTime()).build();
        } else {
            log.info("Message iss not present, creating new...");
            Optional<User> byUsername = userRepository.findByUsername(message.getFrom());
            Optional<Chat> chat = chatRepository.findById(id);
            System.out.println(message);
            chat.ifPresent(value -> byUsername.ifPresent(u -> {
                Message save = messageRepository.save(new Message(message.getId(), message.getText(), u, message.getStatus() != null ? MessageStatus.valueOf(message.getStatus()) :
                        MessageStatus.DELIVERED));
                message.setDateTime(save.getDateTime());
                message.setStatus(save.getStatus().toString());
                message.setId(save.getId());
                chat.get().getMessages().add(save);
                chatRepository.save(chat.get());

            }));
            log.info("Created message: -----------------------");
            log.info(message.toString());
            resp = message;
        }

        NewMessageNotification notification = NewMessageNotification.builder()
                .id(resp.getId())
                .chatId(id)
                .status(resp.getStatus())
                .text(resp.getText())
                .from(resp.getFrom())
                .dateTime(resp.getDateTime())
                .build();

        Optional<Chat> chat = chatRepository.findById(id);
        if(chat.isPresent()){
            User destUser = null;
            for( User u : chat.get().getParticipants()){
                if(!u.getUsername().equals(message.getFrom())){
                    destUser = u;
                    break;
                }
            }

            if(destUser!=null){
                log.info("Sending to /topic/messages/"+destUser.getUsername());
                messagingTemplate.convertAndSend("/topic/messages/"+destUser.getUsername(), notification);
            }
        }

        return resp;
    }
}

