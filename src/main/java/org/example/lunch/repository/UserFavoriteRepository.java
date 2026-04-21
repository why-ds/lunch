package org.example.lunch.repository;

import org.example.lunch.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    List<UserFavorite> findByUserSeq(Long userSeq);
    Optional<UserFavorite> findByUserSeqAndShopSeq(Long userSeq, Long shopSeq);
    boolean existsByUserSeqAndShopSeq(Long userSeq, Long shopSeq);
}