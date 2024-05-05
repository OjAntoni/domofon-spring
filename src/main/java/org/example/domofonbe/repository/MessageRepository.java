package org.example.domofonbe.repository;

import org.example.domofonbe.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
