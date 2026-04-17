package org.example.lunch.repository;

import org.example.lunch.entity.SubwayStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// 🚨 ID 타입을 String에서 Integer로 변경!
public interface SubwayStationRepository extends JpaRepository<SubwayStation, Integer> {

    // 호선 목록 (중복 제거)
    @Query("SELECT DISTINCT s.lineNm FROM SubwayStation s ORDER BY s.lineNm ASC")
    List<String> findAllDistinctLines();

    // 특정 호선의 역 목록
    List<SubwayStation> findByLineNmOrderByStationNmAsc(String lineNm);
}