package org.example.lunch.controller;

import org.example.lunch.entity.Shop;
import org.example.lunch.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.example.lunch.service.KakaoGeoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopRepository shopRepository;
    private final KakaoGeoService kakaoGeoService;

    @GetMapping
    public List<Shop> getShops(
            @RequestParam(required = false) String stationCd,
            @RequestParam(required = false) String foodTypeCd) {
        if (stationCd != null && foodTypeCd != null) {
            return shopRepository.findByStationCdAndFoodTypeCd(stationCd, foodTypeCd);
        } else if (stationCd != null) {
            return shopRepository.findByStationCd(stationCd);
        }
        return shopRepository.findAll();
    }
    /**
     * 랜드마크 반경 검색
     * GET /api/shops/nearby?lat=37.5607&lng=126.9738&radius=500
     */
    @GetMapping("/nearby")
    public List<Shop> getNearbyShops(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "500") int radius,
            @RequestParam(required = false) String foodTypeCd) {
        if (foodTypeCd != null && !foodTypeCd.isEmpty()) {
            return shopRepository.findNearbyShopsByFoodType(lat, lng, radius, foodTypeCd);
        }
        return shopRepository.findNearbyShops(lat, lng, radius);
    }
    /**
     * 가게 단건 등록
     * POST /api/shops/single
     */
    @PostMapping("/single")
    public ResponseEntity<Map<String, Object>> createShop(@RequestBody Shop shop) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 주소로 좌표 변환
            if (shop.getAddress() != null && !shop.getAddress().isEmpty()) {
                Map<String, String> geoInfo = kakaoGeoService.getGeoInfo(shop.getAddress());
                if (geoInfo != null) {
                    if (geoInfo.get("latitude") != null) shop.setLatitude(Double.parseDouble(geoInfo.get("latitude")));
                    if (geoInfo.get("longitude") != null) shop.setLongitude(Double.parseDouble(geoInfo.get("longitude")));
                    shop.setSidoCd(geoInfo.getOrDefault("sido_cd", ""));
                    shop.setGugunCd(geoInfo.getOrDefault("gugun_cd", ""));
                    shop.setDongCd(geoInfo.getOrDefault("dong_cd", ""));
                    shop.setSidoNm(geoInfo.getOrDefault("sido_nm", ""));
                    shop.setGugunNm(geoInfo.getOrDefault("gugun_nm", ""));
                    shop.setDongNm(geoInfo.getOrDefault("dong_nm", ""));
                }
            }
            shop.setCloseYn("N");
            shop.setRegId("ADMIN");
            shop.setRegDt(java.time.LocalDateTime.now());
            shopRepository.save(shop);
            result.put("success", true);
            result.put("message", "등록 완료: " + shop.getShopNm());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "등록 실패: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}