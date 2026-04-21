package org.example.lunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verify")
@Getter
@Setter
@NoArgsConstructor
public class EmailVerify {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verify_seq")
    private Integer verifySeq;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "verified", nullable = false, length = 1)
    private String verified = "N";

    @Column(name = "expire_dt", nullable = false)
    private LocalDateTime expireDt;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;
}