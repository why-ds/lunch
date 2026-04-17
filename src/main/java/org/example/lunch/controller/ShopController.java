package org.example.lunch.controller;

import org.example.lunch.entity.Shop;
import org.example.lunch.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 가게 조회 API
 */
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopRepository shopRepository;

    @GetMapping
    public List<Shop> getShops(@RequestParam(required = false) String stationCd) {
        if (stationCd != null && !stationCd.isEmpty()) {
            return shopRepository.findByStationCd(stationCd);
        }
        return shopRepository.findAll();
    }
}