package org.example.domofonbe.repository;

import org.example.domofonbe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findAllByUsernameContains(String username);
    List<User> findAllByFolderNameIsNull();
}
