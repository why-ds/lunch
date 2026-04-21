package org.example.lunch.repository;

import org.example.lunch.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    // [수정] 역 코드(station_cd)를 기준으로 식당을 찾는 메서드 추가
    List<Shop> findByStationCd(String stationCd);
    /**
     * 반경 내 가게 검색 (미터 단위)
     * 하버사인 공식으로 거리 계산
     */
    @Query(value = "SELECT * FROM shop WHERE " +
            "(6371000 * acos(cos(radians(:lat)) * cos(radians(latitude)) " +
            "* cos(radians(longitude) - radians(:lng)) " +
            "+ sin(radians(:lat)) * sin(radians(latitude)))) <= :radius " +
            "AND latitude IS NOT NULL AND longitude IS NOT NULL",
            nativeQuery = true)
    List<Shop> findNearbyShops(@Param("lat") Double lat,
                               @Param("lng") Double lng,
                               @Param("radius") int radius);
    boolean existsByShopNmAndAddress(String shopNm, String address);
    List<Shop> findByStationCdAndFoodTypeCd(String stationCd, String foodTypeCd);
    @Query(value = "SELECT * FROM shop WHERE " +
            "latitude IS NOT NULL AND longitude IS NOT NULL AND " +
            "food_type_cd = :foodTypeCd AND " +
            "(6371000 * acos(cos(radians(:lat)) * cos(radians(latitude)) " +
            "* cos(radians(longitude) - radians(:lng)) " +
            "+ sin(radians(:lat)) * sin(radians(latitude)))) <= :radius",
            nativeQuery = true)
    List<Shop> findNearbyShopsByFoodType(@Param("lat") Double lat,
                                         @Param("lng") Double lng,
                                         @Param("radius") int radius,
                                         @Param("foodTypeCd") String foodTypeCd);
}