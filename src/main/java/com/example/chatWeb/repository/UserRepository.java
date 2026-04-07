package com.example.chatWeb.repository;

import com.example.chatWeb.entity.User;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(@Email(message = "EMAIL_INVALID") String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(@Email(message = "EMAIL_INVALID") String email);
}
