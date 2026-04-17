package org.example.lunch.controller;

import org.example.lunch.entity.Shop;
import org.example.lunch.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopRepository shopRepository;

    @GetMapping
    public ResponseEntity<List<Shop>> getShops(@RequestParam(required = false) String stationCd) {
        // [수정] 파라미터 이름을 areaCd에서 stationCd로 변경
        if (stationCd != null && !stationCd.trim().isEmpty()) {
            return ResponseEntity.ok(shopRepository.findByStationCd(stationCd.trim()));
        }
        return ResponseEntity.ok(shopRepository.findAll());
    }
}