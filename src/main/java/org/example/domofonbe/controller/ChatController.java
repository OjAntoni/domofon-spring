package org.example.domofonbe.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.domofonbe.dto.ChatConversationResponse;
import org.example.domofonbe.dto.ChatResponse;
import org.example.domofonbe.dto.MessageResponse;
import org.example.domofonbe.dto.UserResponse;
import org.example.domofonbe.mapper.UserMapper;
import org.example.domofonbe.model.Chat;
import org.example.domofonbe.model.Message;
import org.example.domofonbe.model.User;
import org.example.domofonbe.repository.ChatRepository;
import org.example.domofonbe.repository.UserRepository;
import org.example.domofonbe.service.FileService;
import org.example.domofonbe.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.security.Principal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileService fileService;
    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private UserMapper userMapper;

    @GetMapping("/all")
    public ResponseEntity<?> getChatListForUser(@RequestParam(required = false) String chatName, Principal principal) {

        log.info("/all");
        List<ChatResponse> resp;
        User principalUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (chatName == null || chatName.isBlank() || chatName.isEmpty()) {
            resp = new ArrayList<>(chatRepository.findAllByParticipantsUsernames(principal.getName(), chatName).stream().map(
                    c -> {
                        Message message = chatRepository.getLastMessage(c.getId()).orElse(null);
                        return ChatResponse.builder()
                                .unreadCount(chatRepository.countUnreadMessagesInChat(c.getId(), principalUser.getId()))
                                .id(c.getId())
                                .participants(c.getParticipants().stream().map(p -> userMapper.map(p)).toList())
                                .lastMessage(message != null ? new MessageResponse(message.getId(), message.getContent(), message.getAuthor().getUsername(), message.getDateTime(), message.getStatus().toString()) : null)
                                .build();
                    }).toList());
        } else {
            resp = new ArrayList<>(chatRepository.findAllForUser(principal.getName(), chatName).stream().map(
                    c -> {
                        Message message = chatRepository.getLastMessage(c.getId()).orElse(null);
                        return ChatResponse.builder()
                                .unreadCount(chatRepository.countUnreadMessagesInChat(c.getId(), principalUser.getId()))
                                .id(c.getId())
                                .participants(c.getParticipants().stream().map(p -> userMapper.map(p)).toList())
                                .lastMessage(message != null ? new MessageResponse(message.getContent(), message.getAuthor().getUsername(), message.getDateTime()) : null)
                                .build();
                    }).toList());
        }
        ;
        System.out.println(chatName);
        if (chatName != null && !chatName.isEmpty()) {
//            log.info(chatRepository.findUsersByUsernameLikeAndNotInChatsWith(chatName, principal.getName()).toString());
            List<ChatResponse> emptyChats = chatRepository.findUsersByUsernameLikeAndNotInChatsWith(chatName, principal.getName()).stream().map(
                    u ->
                            ChatResponse.builder()
                                    .id(-1)
                                    .participants(List.of(
                                            userMapper.map(u), userMapper.map(principalUser))
                                    )
                                    .lastMessage(null).build()
            ).toList();
            resp.addAll(emptyChats);
//            resp = filterChats(resp);
        }


        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getChat(@PathVariable long id) {
        Optional<Chat> byId = chatRepository.findById(id);
        if (byId.isPresent()) {
            Chat chat = byId.get();
            ChatConversationResponse resp = ChatConversationResponse.builder()
                    .id(chat.getId())
                    .participants(chat.getParticipants().stream().map(p -> userMapper.map(p)).toList())
                    .messages(chat.getMessages().stream().map(m -> new MessageResponse(m.getId(), m.getContent(), m.getAuthor().getUsername(), m.getDateTime(), m.getStatus().toString())).toList())
                    .build();
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}/conversation")
    public ResponseEntity<ChatConversationResponse> getChatConversation(@PathVariable long id) {
        Optional<Chat> byId = chatRepository.findById(id);
        if (byId.isPresent()) {
            Chat chat = byId.get();
            ChatConversationResponse resp = ChatConversationResponse.builder()
                    .id(chat.getId())
                    .participants(chat.getParticipants().stream().map(p -> userMapper.map(p)).toList())
                    .messages(chat.getMessages().stream().map(m -> new MessageResponse(m.getContent(), m.getAuthor().getUsername(), m.getDateTime())).toList())
                    .build();
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    public ResponseEntity<?> createChat(@RequestParam String to, Principal principal) {
        log.info("/chat (creating new)");
        List<Chat> chats = chatRepository.findAllByParticipantsUsernames(to, principal.getName());
        Chat save;
        if (chats.isEmpty()) {
            User myself = userRepository.findByUsername(principal.getName()).orElseThrow();
            User toUser = userRepository.findByUsername(to).orElseThrow();

            save = chatRepository.save(Chat.builder().participants(
                    List.of(myself, toUser)
            ).build());
        } else if (chats.size() == 1) {
            save = chats.getFirst();
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                ChatResponse.builder()
                        .id(save.getId())
                        .participants(save.getParticipants().stream().map(p -> userMapper.map(p)).toList())
                        .lastMessage(new MessageResponse("TODO LAST MESSAGE")).build(), HttpStatus.OK);
    }


}
