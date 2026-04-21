package org.example.lunch.controller;

import org.example.lunch.config.JwtUtil;
import org.example.lunch.entity.Users;
import lombok.RequiredArgsConstructor;
import org.example.lunch.repository.EmailVerifyRepository;
import org.example.lunch.repository.UsersRepository;
import org.example.lunch.service.EmailService;
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
    private final EmailService emailService;
    private final EmailVerifyRepository emailVerifyRepository;

    /**
     * 이메일 인증코드 발송
     * POST /api/auth/send-code
     */
    @PostMapping("/send-code")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "이메일을 입력해주세요.");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            emailService.sendVerifyCode(email);
            result.put("success", true);
            result.put("message", "인증코드가 발송되었습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "메일 발송 실패: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 이메일 인증코드 확인
     * POST /api/auth/verify-code
     */
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String email = request.get("email");
        String code = request.get("code");

        boolean verified = emailService.verifyCode(email, code);
        result.put("success", verified);
        result.put("message", verified ? "인증 완료!" : "인증코드가 올바르지 않거나 만료되었습니다.");
        return ResponseEntity.ok(result);
    }

    /**
     * 회원가입 (이메일 인증 필수)
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();

        String userId = request.get("userId");
        String password = request.get("password");
        String nickname = request.get("nickname");
        String email = request.get("email");

        if (userId == null || userId.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "아이디를 입력해주세요.");
            return ResponseEntity.badRequest().body(result);
        }
        if (password == null || password.length() < 4) {
            result.put("success", false);
            result.put("message", "비밀번호는 4자 이상이어야 합니다.");
            return ResponseEntity.badRequest().body(result);
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "닉네임을 입력해주세요.");
            return ResponseEntity.badRequest().body(result);
        }
        if (email == null || email.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "이메일을 입력해주세요.");
            return ResponseEntity.badRequest().body(result);
        }
        if (userRepository.findByUserId(userId).isPresent()) {
            result.put("success", false);
            result.put("message", "이미 사용중인 아이디입니다.");
            return ResponseEntity.badRequest().body(result);
        }

        // 이메일 인증 확인
        boolean emailVerified = emailVerifyRepository
                .findTopByEmailAndVerifiedOrderByRegDtDesc(email, "Y")
                .isPresent();
        if (!emailVerified) {
            result.put("success", false);
            result.put("message", "이메일 인증을 완료해주세요.");
            return ResponseEntity.badRequest().body(result);
        }

        Users user = new Users();
        user.setUserId(userId.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname.trim());
        user.setEmail(email.trim());
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
        result.put("userNm", user.getNickname());
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
        admin.setNickname("관리자");
        admin.setRole("ADMIN");
        admin.setUseYn("Y");
        admin.setRegDt(LocalDateTime.now());

        userRepository.save(admin);

        result.put("success", true);
        result.put("message", "admin 계정 생성 완료 (비밀번호: admin123)");
        return ResponseEntity.ok(result);
    }
}