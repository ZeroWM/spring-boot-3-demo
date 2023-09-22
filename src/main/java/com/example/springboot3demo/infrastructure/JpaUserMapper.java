package com.example.springboot3demo.infrastructure;

import com.example.springboot3demo.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserMapper extends JpaRepository<User, String> {
}
