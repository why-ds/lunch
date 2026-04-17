package org.example.lunch.repository;

import org.example.lunch.entity.Landmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LandmarkRepository extends JpaRepository<Landmark, Integer> {
    List<Landmark> findByUseYnOrderBySortOrd(String useYn);
}