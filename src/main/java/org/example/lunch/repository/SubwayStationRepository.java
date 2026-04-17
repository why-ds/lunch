package org.example.lunch.repository;

import org.example.lunch.entity.SubwayStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubwayStationRepository extends JpaRepository<SubwayStation, String> {

    /** 호선 필터용: DB에 있는 모든 호선명을 중복 없이 오름차순으로 조회 */
    @Query("SELECT DISTINCT s.lineNm FROM SubwayStation s ORDER BY s.lineNm ASC")
    List<String> findAllDistinctLines();

    /** 역명 필터용: 특정 호선에 속한 역 목록을 조회 */
    List<SubwayStation> findByLineNmOrderByStationNmAsc(String lineNm);
}