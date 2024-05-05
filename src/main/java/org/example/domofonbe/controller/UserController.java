package org.example.domofonbe.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domofonbe.config.JwtUtil;
import org.example.domofonbe.config.MyUserDetailsService;
import org.example.domofonbe.dto.UpdateUserRequest;
import org.example.domofonbe.dto.UserResponse;
import org.example.domofonbe.mapper.UserMapper;
import org.example.domofonbe.model.User;
import org.example.domofonbe.repository.UserRepository;
import org.example.domofonbe.service.FileService;
import org.example.domofonbe.util.FileUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {
    private UserRepository userRepository;
    private FileService fileService;
    private UserMapper userMapper;
    private FileUtil fileUtil;
    private JwtUtil jwtUtil;
    private MyUserDetailsService myUserDetailsService;
    private ObjectMapper mapper;

    @PostMapping("/profile/image")
    public ResponseEntity<UserResponse> uploadProfileImage(@RequestParam("image") MultipartFile file, Principal principal) {
        Optional<User> byUsername = userRepository.findByUsername(principal.getName());
        if (byUsername.isPresent()) {
            User user = byUsername.get();
            try {
                String fileNameForUser = fileUtil.getFileNameForUser(user);
                String bucketNameForUser = fileUtil.getBucketNameForUser(user);
                String fileName = fileService.saveObject(file.getInputStream(), fileNameForUser, bucketNameForUser, "image/jpg");
                user.setProfilePhoto(fileName);
                userRepository.save(user);
                UserResponse resp = userMapper.map(user);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            } catch (IOException e) {
                log.warn(Arrays.toString(e.getStackTrace()));
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<UserResponse> getInfoAboutProfile(@RequestParam(name = "id", required = false, defaultValue = "0") long userId, Principal principal) {
        if (userId > 0) {
            //TODO
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        } else {
            String name = principal.getName();
            Optional<User> byUsername = userRepository.findByUsername(name);
            if (byUsername.isPresent()) {
                User user = byUsername.get();
                return new ResponseEntity<>(userMapper.map(user), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @PostMapping
    public ResponseEntity<?> updateUserProfile(@RequestBody @Valid UpdateUserRequest request, Principal principal) throws JsonProcessingException {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        if (userRepository.findByUsername(request.getUsername()).isEmpty())
            user.setUsername(request.getUsername());
        else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        userRepository.save(user);

        UserDetails userDetails = myUserDetailsService.loadUserByUsername(request.getUsername());
        String jwt = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        UserResponse resp = userMapper.map(user);

        String s = mapper.writeValueAsString(resp);
        ObjectNode root = (ObjectNode) mapper.readTree(s);
        root.put("token", jwt);
        root.put("refreshToken", refreshToken);
        String modifiedResp = mapper.writeValueAsString(root);

        return new ResponseEntity<>(modifiedResp, HttpStatus.OK);
    }
}
