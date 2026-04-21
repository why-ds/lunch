package org.example.lunch.controller;

import org.example.lunch.entity.*;
import org.example.lunch.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserActionController {

    private final UserFavoriteRepository favoriteRepository;
    private final UserBlacklistRepository blacklistRepository;
    private final UsersRepository usersRepository;
    private final ShopRepository shopRepository;

    /**
     * 현재 사용자의 userSeq 조회
     */
    private Long getUserSeq(Authentication auth) {
        String userId = auth.getName();
        return usersRepository.findByUserId(userId)
                .map(Users::getUserSeq)
                .orElse(null);
    }

    // ============ 즐겨찾기 ============

    /**
     * 즐겨찾기 목록 조회
     * GET /api/user/favorites
     */
    @GetMapping("/favorites")
    public List<Map<String, Object>> getFavorites(Authentication auth) {
        Long userSeq = getUserSeq(auth);
        if (userSeq == null) return List.of();

        return favoriteRepository.findByUserSeq(userSeq).stream().map(fav -> {
            Map<String, Object> map = new HashMap<>();
            map.put("favSeq", fav.getFavSeq());
            map.put("shopSeq", fav.getShopSeq());
            shopRepository.findById(fav.getShopSeq()).ifPresent(shop -> {
                map.put("shopNm", shop.getShopNm());
                map.put("address", shop.getAddress());
                map.put("rmk", shop.getRmk());
                map.put("foodTypeCd", shop.getFoodTypeCd());
            });
            map.put("regDt", fav.getRegDt());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 즐겨찾기 추가/해제 (토글)
     * POST /api/user/favorites/toggle
     */
    @PostMapping("/favorites/toggle")
    public ResponseEntity<Map<String, Object>> toggleFavorite(@RequestBody Map<String, Long> request, Authentication auth) {
        Map<String, Object> result = new HashMap<>();
        Long userSeq = getUserSeq(auth);
        Long shopSeq = request.get("shopSeq");

        if (userSeq == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(result);
        }

        Optional<UserFavorite> existing = favoriteRepository.findByUserSeqAndShopSeq(userSeq, shopSeq);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            result.put("success", true);
            result.put("action", "removed");
            result.put("message", "즐겨찾기에서 해제했습니다.");
        } else {
            UserFavorite fav = new UserFavorite();
            fav.setUserSeq(userSeq);
            fav.setShopSeq(shopSeq);
            fav.setRegDt(LocalDateTime.now());
            favoriteRepository.save(fav);
            result.put("success", true);
            result.put("action", "added");
            result.put("message", "즐겨찾기에 추가했습니다.");
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 가게의 즐겨찾기 여부 확인
     * GET /api/user/favorites/check?shopSeq=1
     */
    @GetMapping("/favorites/check")
    public Map<String, Boolean> checkFavorite(@RequestParam Long shopSeq, Authentication auth) {
        Long userSeq = getUserSeq(auth);
        boolean isFavorite = userSeq != null && favoriteRepository.existsByUserSeqAndShopSeq(userSeq, shopSeq);
        return Map.of("isFavorite", isFavorite);
    }

    // ============ 블랙리스트 ============

    /**
     * 블랙리스트 목록 조회
     * GET /api/user/blacklist
     */
    @GetMapping("/blacklist")
    public List<Map<String, Object>> getBlacklist(Authentication auth) {
        Long userSeq = getUserSeq(auth);
        if (userSeq == null) return List.of();

        return blacklistRepository.findByUserSeq(userSeq).stream().map(black -> {
            Map<String, Object> map = new HashMap<>();
            map.put("blackSeq", black.getBlackSeq());
            map.put("shopSeq", black.getShopSeq());
            shopRepository.findById(black.getShopSeq()).ifPresent(shop -> {
                map.put("shopNm", shop.getShopNm());
                map.put("address", shop.getAddress());
                map.put("rmk", shop.getRmk());
            });
            map.put("regDt", black.getRegDt());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 블랙리스트 추가/해제 (토글)
     * POST /api/user/blacklist/toggle
     */
    @PostMapping("/blacklist/toggle")
    public ResponseEntity<Map<String, Object>> toggleBlacklist(@RequestBody Map<String, Long> request, Authentication auth) {
        Map<String, Object> result = new HashMap<>();
        Long userSeq = getUserSeq(auth);
        Long shopSeq = request.get("shopSeq");

        if (userSeq == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(result);
        }

        Optional<UserBlacklist> existing = blacklistRepository.findByUserSeqAndShopSeq(userSeq, shopSeq);
        if (existing.isPresent()) {
            blacklistRepository.delete(existing.get());
            result.put("success", true);
            result.put("action", "removed");
            result.put("message", "블랙리스트에서 해제했습니다.");
        } else {
            UserBlacklist black = new UserBlacklist();
            black.setUserSeq(userSeq);
            black.setShopSeq(shopSeq);
            black.setRegDt(LocalDateTime.now());
            blacklistRepository.save(black);
            result.put("success", true);
            result.put("action", "added");
            result.put("message", "블랙리스트에 추가했습니다.");
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 가게의 블랙리스트 여부 확인
     * GET /api/user/blacklist/check?shopSeq=1
     */
    @GetMapping("/blacklist/check")
    public Map<String, Boolean> checkBlacklist(@RequestParam Long shopSeq, Authentication auth) {
        Long userSeq = getUserSeq(auth);
        boolean isBlacklisted = userSeq != null && blacklistRepository.existsByUserSeqAndShopSeq(userSeq, shopSeq);
        return Map.of("isBlacklisted", isBlacklisted);
    }

    /**
     * 블랙리스트 shopSeq 목록 (랜덤 뽑기에서 제외용)
     * GET /api/user/blacklist/ids
     */
    @GetMapping("/blacklist/ids")
    public List<Long> getBlacklistIds(Authentication auth) {
        Long userSeq = getUserSeq(auth);
        if (userSeq == null) return List.of();
        return blacklistRepository.findByUserSeq(userSeq).stream()
                .map(UserBlacklist::getShopSeq)
                .collect(Collectors.toList());
    }
}