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
     * 회원가입
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();

        String userId = request.get("userId");
        String password = request.get("password");
        String userNm = request.get("userNm");

        // 필수값 체크
        if (userId == null || userId.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "아이디를 입력해주세요.");
            return ResponseEntity.badRequest().body(result);
        }
        if (password == null || password.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "비밀번호를 입력해주세요.");
            return ResponseEntity.badRequest().body(result);
        }
        if (userNm == null || userNm.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "이름을 입력해주세요.");
            return ResponseEntity.badRequest().body(result);
        }

        // 아이디 중복 체크
        if (userRepository.findByUserId(userId).isPresent()) {
            result.put("success", false);
            result.put("message", "이미 사용중인 아이디입니다.");
            return ResponseEntity.badRequest().body(result);
        }

        // 비밀번호 길이 체크
        if (password.length() < 4) {
            result.put("success", false);
            result.put("message", "비밀번호는 4자 이상이어야 합니다.");
            return ResponseEntity.badRequest().body(result);
        }

        // 사용자 생성
        Users user = new Users();
        user.setUserId(userId.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setUserNm(userNm.trim());
        user.setRole("USER");
        user.setUseYn("Y");
        user.setRegDt(LocalDateTime.now());

        userRepository.save(user);

        result.put("success", true);
        result.put("message", "회원가입 완료! 로그인해주세요.");
        return ResponseEntity.ok(result);
    }

    /**
     * 아이디 중복 체크
     * GET /api/auth/check?userId=test
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkUserId(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        boolean exists = userRepository.findByUserId(userId).isPresent();
        result.put("exists", exists);
        result.put("message", exists ? "이미 사용중인 아이디입니다." : "사용 가능한 아이디입니다.");
        return ResponseEntity.ok(result);
    }

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