package org.example.lunch.repository;

import org.example.lunch.entity.EmailVerify;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerifyRepository extends JpaRepository<EmailVerify, Integer> {
    Optional<EmailVerify> findTopByEmailAndVerifiedOrderByRegDtDesc(String email, String verified);
}