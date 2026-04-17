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

    @GetMapping
    public List<Shop> getShops(@RequestParam(required = false) String stationCd) {
        if (stationCd != null && !stationCd.trim().isEmpty()) {
            return shopRepository.findByStationCd(stationCd.trim());
        }
        return shopRepository.findAll();
    }
}