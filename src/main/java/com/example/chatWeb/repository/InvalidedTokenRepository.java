package com.example.chatWeb.repository;

import com.example.chatWeb.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidedTokenRepository extends JpaRepository<InvalidatedToken, String> {
}
