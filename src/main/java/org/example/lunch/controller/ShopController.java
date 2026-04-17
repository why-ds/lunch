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
    public ResponseEntity<List<Shop>> getShops(@RequestParam(required = false) String stationCd) {
        // [수정] 파라미터 이름을 areaCd에서 stationCd로 변경
        if (stationCd != null && !stationCd.trim().isEmpty()) {
            return ResponseEntity.ok(shopRepository.findByStationCd(stationCd.trim()));
        }
        return ResponseEntity.ok(shopRepository.findAll());
    }
    /**
     * 랜드마크 반경 검색
     * GET /api/shops/nearby?lat=37.5607&lng=126.9738&radius=500
     */
    @GetMapping("/nearby")
    public List<Shop> getNearbyShops(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "500") int radius) {
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