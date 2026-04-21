package org.example.lunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_blacklist")
@Getter
@Setter
@NoArgsConstructor
public class UserBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "black_seq")
    private Integer blackSeq;

    @Column(name = "user_seq", nullable = false)
    private Long userSeq;

    @Column(name = "shop_seq", nullable = false)
    private Long shopSeq;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;
}