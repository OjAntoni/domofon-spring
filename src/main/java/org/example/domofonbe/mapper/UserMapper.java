package org.example.domofonbe.mapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domofonbe.dto.UserResponse;
import org.example.domofonbe.model.User;
import org.example.domofonbe.repository.UserRepository;
import org.example.domofonbe.service.FileService;
import org.example.domofonbe.util.FileUtil;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class UserMapper {
    private UserRepository userRepository;
    private FileService fileService;
    private FileUtil fileUtil;

    public UserResponse map(User user) {
        String imageUrl = null;
        if(user.getFolderName()!=null && user.getProfilePhoto()!=null){
            imageUrl = fileService.getObjectUrl(
                    user.getProfilePhoto(),
                    user.getFolderName(),
                    "image/jpg");
//            String replace = imageUrl.replace("127.0.0.1", "192.168.1.101");
//            log.info(replace);
        }
        return UserResponse.builder()
                .username(user.getUsername())
                .imageUrl(imageUrl).build();
    }

    public UserResponse map(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return map(user);
    }
}
