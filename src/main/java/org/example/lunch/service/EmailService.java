package org.example.lunch.service;

import org.example.lunch.entity.EmailVerify;
import org.example.lunch.repository.EmailVerifyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerifyRepository emailVerifyRepository;

    /**
     * 인증코드 발송 (6자리 숫자)
     */
    public void sendVerifyCode(String email) {
        String code = String.format("%06d", new Random().nextInt(1000000));

        // DB에 저장 (5분 유효)
        EmailVerify verify = new EmailVerify();
        verify.setEmail(email);
        verify.setCode(code);
        verify.setVerified("N");
        verify.setExpireDt(LocalDateTime.now().plusMinutes(5));
        verify.setRegDt(LocalDateTime.now());
        emailVerifyRepository.save(verify);

        // 메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[점심뭐먹지] 이메일 인증코드");
        message.setText("인증코드: " + code + "\n\n5분 이내에 입력해주세요.");
        mailSender.send(message);
    }

    /**
     * 인증코드 검증
     */
    public boolean verifyCode(String email, String code) {
        return emailVerifyRepository
                .findTopByEmailAndVerifiedOrderByRegDtDesc(email, "N")
                .filter(v -> v.getCode().equals(code))
                .filter(v -> v.getExpireDt().isAfter(LocalDateTime.now()))
                .map(v -> {
                    v.setVerified("Y");
                    emailVerifyRepository.save(v);
                    return true;
                })
                .orElse(false);
    }
}