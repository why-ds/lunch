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
     * 전체 가게 목록 조회
     * GET /api/shops
     */
    @GetMapping
    public List<Shop> getShops() {
        return shopRepository.findAll();
    }
}