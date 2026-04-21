package org.example.lunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_favorite")
@Getter
@Setter
@NoArgsConstructor
public class UserFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fav_seq")
    private Integer favSeq;

    @Column(name = "user_seq", nullable = false)
    private Long userSeq;

    @Column(name = "shop_seq", nullable = false)
    private Long shopSeq;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;
}