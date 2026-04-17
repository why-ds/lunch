package org.example.lunch.repository;

import org.example.lunch.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    // [수정] 역 코드(station_cd)를 기준으로 식당을 찾는 메서드 추가
    List<Shop> findByStationCd(String stationCd);
}