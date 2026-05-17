package com.example.chatWeb.repository;

import com.example.chatWeb.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(@Email(message = "EMAIL_INVALID") String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(@Email(message = "EMAIL_INVALID") String email);

    @Query("""
    SELECT u
    FROM User u
    WHERE u.id <> :currentUserId
      AND (
          LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
""")
    List<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("currentUserId") Long currentUserId
    );
}
