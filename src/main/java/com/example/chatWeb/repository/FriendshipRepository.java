package com.example.chatWeb.repository;

import com.example.chatWeb.entity.Friendship;
import com.example.chatWeb.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE f.user.id = :userId1 AND f.friend.id = :userId2")
    Optional<Friendship> findBySubIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT u FROM User u WHERE u.id IN (" +
            "  SELECT f.friend.id FROM Friendship f WHERE f.user.id = :userId AND f.status = 'ACCEPTED'" +
            "  UNION " +
            "  SELECT f.user.id FROM Friendship f WHERE f.friend.id = :userId AND f.status = 'ACCEPTED'" +
            ")")
    List<User> findAcceptedFriendsByUserId(@Param("userId") Long userId);

    @Query("SELECT f.user FROM Friendship f WHERE f.friend.id = :userId AND f.status = 'PENDING'")
    List<User> findPendingRequestsByUserId(@Param("userId") Long userId);
}
