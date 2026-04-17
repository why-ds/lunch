package org.example.lunch.controller;

import org.example.lunch.entity.Shop;
import org.example.lunch.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopRepository shopRepository;

    /**
     * 가게 조회 API (역 코드 기준 필터링 지원)
     */
    @GetMapping
    public List<Shop> getShops(@RequestParam(required = false) String stationCd) {
        // 파라미터가 null이 아니면서, 공백으로만 이루어진 문자열("  ")인 경우도 방어하기 위해 trim() 적용
        if (stationCd != null && !stationCd.trim().isEmpty()) {
            return shopRepository.findByStationCd(stationCd.trim());
        }
        // 파라미터가 없으면 전체 조회
        return shopRepository.findAll();
    }
}