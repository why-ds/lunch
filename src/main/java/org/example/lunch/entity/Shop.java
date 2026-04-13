package org.example.lunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 가게 엔티티
 */
@Entity
@Table(name = "shop")
@Getter
@Setter
@NoArgsConstructor
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_seq")
    private Integer shopSeq;

    @Column(name = "area_cd", nullable = false, length = 20)
    private String areaCd;

    @Column(name = "food_type_cd", nullable = false, length = 20)
    private String foodTypeCd;

    @Column(name = "biz_hour_cd", length = 20)
    private String bizHourCd;

    @Column(name = "shop_nm", nullable = false, length = 200)
    private String shopNm;

    @Column(name = "close_yn", nullable = false, length = 1)
    private String closeYn = "N";

    @Column(name = "walk_dist_cd", length = 20)
    private String walkDistCd;

    @Column(name = "rmk", length = 500)
    private String rmk;

    @Column(name = "reg_id", nullable = false, length = 50)
    private String regId;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "mod_id", length = 50)
    private String modId;

    @Column(name = "mod_dt")
    private LocalDateTime modDt;
}