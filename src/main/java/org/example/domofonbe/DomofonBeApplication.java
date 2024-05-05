package org.example.domofonbe;

import org.example.domofonbe.model.Message;
import org.example.domofonbe.model.MessageStatus;
import org.example.domofonbe.model.User;
import org.example.domofonbe.repository.MessageRepository;
import org.example.domofonbe.repository.UserRepository;
import org.example.domofonbe.service.FileService;
import org.example.domofonbe.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@SpringBootApplication
public class DomofonBeApplication implements CommandLineRunner {
    @Autowired
    FileService fileService;
    @Autowired
    FileUtil fileUtil;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MessageRepository messageRepository;

    public static void main(String[] args) {
        SpringApplication.run(DomofonBeApplication.class, args);
    }

    @Override
    public void run(String... args){
        System.out.println(userRepository.findAllByFolderNameIsNull());
        List<User> list = userRepository.findAllByFolderNameIsNull().stream()
                .peek(u -> u.setFolderName(fileUtil.getBucketNameForUser(u)))
                .toList();
        userRepository.saveAll(list);

        List<Message> msgs = messageRepository.findAll().stream().peek(m -> {
            if (m.getStatus() == null) m.setStatus(MessageStatus.DELIVERED);
        }).toList();
        messageRepository.saveAll(msgs);
    }
}
