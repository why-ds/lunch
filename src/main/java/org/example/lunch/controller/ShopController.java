package org.example.lunch.controller;

import org.example.lunch.entity.Shop;
import org.example.lunch.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 가게 조회 API
 */
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopRepository shopRepository;

    /**
     * 가게 목록 조회 (구역 필터 가능)
     * GET /api/shops
     * GET /api/shops?areaCd=A01
     */
    @GetMapping
    public List<Shop> getShops(@RequestParam(required = false) String areaCd) {
        if (areaCd != null && !areaCd.isEmpty()) {
            return shopRepository.findByAreaCd(areaCd);
        }
        return shopRepository.findAll();
    }
}