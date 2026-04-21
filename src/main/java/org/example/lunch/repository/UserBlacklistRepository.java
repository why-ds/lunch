package org.example.lunch.repository;

import org.example.lunch.entity.UserBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlacklistRepository extends JpaRepository<UserBlacklist, Long> {
    List<UserBlacklist> findByUserSeq(Long userSeq);
    Optional<UserBlacklist> findByUserSeqAndShopSeq(Long userSeq, Long shopSeq);
    boolean existsByUserSeqAndShopSeq(Long userSeq, Long shopSeq);
}