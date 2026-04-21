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

    /** 가게 일련번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_seq")
    private Long shopSeq;

    /** 가게명 */
    @Column(name = "shop_nm", nullable = false, length = 200)
    private String shopNm;

    /** 지하철역코드 */
    @Column(name = "station_cd", length = 20)
    private String stationCd;

    /** 음식종류코드 */
    @Column(name = "food_type_cd", nullable = false, length = 20)
    private String foodTypeCd;

    /** 주소 */
    @Column(name = "address", length = 500)
    private String address;

    /** 시도코드 */
    @Column(name = "sido_cd", length = 20)
    private String sidoCd;

    /** 시군구코드 */
    @Column(name = "gugun_cd", length = 20)
    private String gugunCd;

    /** 동코드 */
    @Column(name = "dong_cd", length = 20)
    private String dongCd;

    @Column(name = "sido_nm", length = 50)
    private String sidoNm;

    @Column(name = "gugun_nm", length = 50)
    private String gugunNm;

    @Column(name = "dong_nm", length = 50)
    private String dongNm;

    /** 위도 */
    @Column(name = "latitude")
    private Double latitude;

    /** 경도 */
    @Column(name = "longitude")
    private Double longitude;

    /** 휴폐업여부 */
    @Column(name = "close_yn", nullable = false, length = 1)
    private String closeYn = "N";

    /** 비고 */
    @Column(name = "rmk", length = 500)
    private String rmk;

    /** 입력자 */
    @Column(name = "reg_id", nullable = false, length = 50)
    private String regId;

    /** 입력일시 */
    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    /** 변경자 */
    @Column(name = "mod_id", length = 50)
    private String modId;

    /** 변경일시 */
    @Column(name = "mod_dt")
    private LocalDateTime modDt;
}