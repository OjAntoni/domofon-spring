package org.example.domofonbe.util;

import org.example.domofonbe.model.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FileUtil {

    public String getBucketNameForUser(User user) {
        if (user.getFolderName() != null) return user.getFolderName();
        else {
            StringBuilder sb = new StringBuilder();
            sb.append("user-");
            sb.append(user.getId()).append("-");
            sb.append(UUID.randomUUID());
            return sb.toString();
        }
    }

    public String getFileNameForUser(User user) {
        StringBuilder sb = new StringBuilder("file-");
        sb.append(UUID.randomUUID());
        return sb.toString();
    }
}
