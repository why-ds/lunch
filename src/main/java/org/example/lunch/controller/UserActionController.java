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
    /**
     * 내 정보 조회
     * GET /api/user/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(Authentication auth) {
        Map<String, Object> result = new HashMap<>();
        String userId = auth.getName();
        usersRepository.findByUserId(userId).ifPresent(user -> {
            result.put("userId", user.getUserId());
            result.put("nickname", user.getNickname());
            result.put("email", user.getEmail());
            result.put("regDt", user.getRegDt().toString());
        });
        return ResponseEntity.ok(result);
    }

    /**
     * 전체 가게 목록 + 즐겨찾기 여부 (즐겨찾기 우선 정렬)
     * GET /api/user/shops-with-favorite
     */
    @GetMapping("/shops-with-favorite")
    public List<Map<String, Object>> getShopsWithFavorite(Authentication auth) {
        Long userSeq = getUserSeq(auth);
        List<Shop> allShops = shopRepository.findAll();

        // 즐겨찾기 shopSeq 목록
        List<Long> favShopSeqs = userSeq != null
                ? favoriteRepository.findByUserSeq(userSeq).stream()
                .map(UserFavorite::getShopSeq).collect(Collectors.toList())
                : List.of();

        // 가게 목록 + 즐겨찾기 여부
        List<Map<String, Object>> result = allShops.stream().map(shop -> {
            Map<String, Object> map = new HashMap<>();
            map.put("shopSeq", shop.getShopSeq());
            map.put("shopNm", shop.getShopNm());
            map.put("address", shop.getAddress());
            map.put("rmk", shop.getRmk());
            map.put("foodTypeCd", shop.getFoodTypeCd());
            map.put("stationCd", shop.getStationCd());
            map.put("isFavorite", favShopSeqs.contains(shop.getShopSeq()));
            return map;
        }).collect(Collectors.toList());

        // 즐겨찾기 우선 정렬
        result.sort((a, b) -> {
            boolean aFav = (boolean) a.get("isFavorite");
            boolean bFav = (boolean) b.get("isFavorite");
            if (aFav && !bFav) return -1;
            if (!aFav && bFav) return 1;
            return ((String) a.get("shopNm")).compareTo((String) b.get("shopNm"));
        });

        return result;
    }
    /**
     * 전체 가게 목록 + 블랙리스트 여부 (블랙리스트 우선 정렬)
     * GET /api/user/shops-with-blacklist
     */
    @GetMapping("/shops-with-blacklist")
    public List<Map<String, Object>> getShopsWithBlacklist(Authentication auth) {
        Long userSeq = getUserSeq(auth);
        List<Shop> allShops = shopRepository.findAll();

        List<Long> blackShopSeqs = userSeq != null
                ? blacklistRepository.findByUserSeq(userSeq).stream()
                .map(UserBlacklist::getShopSeq).collect(Collectors.toList())
                : List.of();

        List<Map<String, Object>> result = allShops.stream().map(shop -> {
            Map<String, Object> map = new HashMap<>();
            map.put("shopSeq", shop.getShopSeq());
            map.put("shopNm", shop.getShopNm());
            map.put("address", shop.getAddress());
            map.put("rmk", shop.getRmk());
            map.put("foodTypeCd", shop.getFoodTypeCd());
            map.put("stationCd", shop.getStationCd());
            map.put("isBlacklisted", blackShopSeqs.contains(shop.getShopSeq()));
            return map;
        }).collect(Collectors.toList());

        result.sort((a, b) -> {
            boolean aBlack = (boolean) a.get("isBlacklisted");
            boolean bBlack = (boolean) b.get("isBlacklisted");
            if (aBlack && !bBlack) return -1;
            if (!aBlack && bBlack) return 1;
            return ((String) a.get("shopNm")).compareTo((String) b.get("shopNm"));
        });

        return result;
    }
    /**
     * 닉네임 수정
     * PUT /api/user/nickname
     */
    @PutMapping("/nickname")
    public ResponseEntity<Map<String, Object>> updateNickname(@RequestBody Map<String, String> request, Authentication auth) {
        Map<String, Object> result = new HashMap<>();
        String newNickname = request.get("nickname");

        if (newNickname == null || newNickname.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "닉네임을 입력해주세요.");
            return ResponseEntity.badRequest().body(result);
        }

        // 중복 체크
        if (usersRepository.findByNickname(newNickname.trim()).isPresent()) {
            result.put("success", false);
            result.put("message", "이미 사용중인 닉네임입니다.");
            return ResponseEntity.badRequest().body(result);
        }

        String userId = auth.getName();
        usersRepository.findByUserId(userId).ifPresent(user -> {
            user.setNickname(newNickname.trim());
            usersRepository.save(user);
        });

        // localStorage도 업데이트하도록 새 닉네임 반환
        result.put("success", true);
        result.put("message", "닉네임이 변경되었습니다.");
        result.put("nickname", newNickname.trim());
        return ResponseEntity.ok(result);
    }
}