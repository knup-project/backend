package com.knupbackend.quiz.repository;

import com.knupbackend.quiz.entity.Quiz;
import com.knupbackend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Page<Quiz> findByUser(User user, Pageable pageable);
}
