package org.example.domofonbe.repository;

import org.example.domofonbe.model.Chat;
import org.example.domofonbe.model.Message;
import org.example.domofonbe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("SELECT c FROM Chat c JOIN c.participants p1 JOIN c.participants p2 WHERE p1.username = :username1 AND p2.username != :username1 AND p2.username LIKE %:username2%")
    List<Chat> findAllByParticipantsUsernames(@Param("username1") String myself, @Param("username2") String username2);

    @Query("SELECT c FROM Chat c JOIN c.participants p1 JOIN c.participants p2 WHERE p1.username = :username1 AND p2.username != :username1 AND p2.username LIKE %:username2%")
    List<Chat> findAllForUser(@Param("username1") String myself, @Param("username2") String username2);

    @Query("SELECT m FROM Chat c JOIN c.messages m WHERE c.id = :chatId ORDER BY m.dateTime DESC LIMIT 1")
    Optional<Message> getLastMessage(@Param("chatId") long chatId);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:username2% AND NOT EXISTS (SELECT c FROM Chat c JOIN c.participants p WHERE p.username = :username1 AND u MEMBER OF c.participants)")
    List<User> findUsersByUsernameLikeAndNotInChatsWith(@Param("username2") String username2, @Param("username1") String username1);

    @Query(value = "select count(*) from (select * from message m join (select * from chat_messages cm where cm.chat_id = :id) as cc on m.id = cc.messages_id and m.status not LIKE 'READ') as mc where author_id != :userId", nativeQuery = true)
    long countUnreadMessagesInChat(@Param("id") long id, long userId);


}
