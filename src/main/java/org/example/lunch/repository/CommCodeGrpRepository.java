package org.example.lunch.repository;

import org.example.lunch.entity.CommCodeGrp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 공통코드 그룹 Repository
 * - JpaRepository 상속만으로 기본 CRUD 메서드 자동 제공
 * - findAll(), findById(), save(), delete() 등
 */
@Repository
public interface CommCodeGrpRepository extends JpaRepository<CommCodeGrp, Long> {
}