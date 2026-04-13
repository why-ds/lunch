package org.example.lunch.repository;

import org.example.lunch.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 가게 Repository
 */
@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {
}