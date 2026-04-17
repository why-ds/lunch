package org.example.lunch.controller;

import org.example.lunch.config.JwtUtil;
import org.example.lunch.entity.Users;
import lombok.RequiredArgsConstructor;
import org.example.lunch.repository.UsersRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 인증 API (로그인/회원가입)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();

        String userId = request.get("userId");
        String password = request.get("password");

        Users user = userRepository.findByUserId(userId).orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "아이디 또는 비밀번호가 틀렸습니다.");
            return ResponseEntity.status(401).body(result);
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getRole());

        result.put("success", true);
        result.put("token", token);
        result.put("userId", user.getUserId());
        result.put("userNm", user.getUserNm());
        result.put("role", user.getRole());

        return ResponseEntity.ok(result);
    }

    /**
     * 관리자 계정 초기 생성 (한번만 쓰고 삭제해도 됨)
     * POST /api/auth/init
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initAdmin() {
        Map<String, Object> result = new HashMap<>();

        if (userRepository.findByUserId("admin").isPresent()) {
            result.put("success", false);
            result.put("message", "이미 admin 계정이 존재합니다.");
            return ResponseEntity.ok(result);
        }

        Users admin = new Users();
        admin.setUserId("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setUserNm("관리자");
        admin.setRole("ADMIN");
        admin.setUseYn("Y");
        admin.setRegDt(LocalDateTime.now());

        userRepository.save(admin);

        result.put("success", true);
        result.put("message", "admin 계정 생성 완료 (비밀번호: admin123)");
        return ResponseEntity.ok(result);
    }
}