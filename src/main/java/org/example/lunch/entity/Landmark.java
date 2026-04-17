package org.example.lunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 랜드마크 엔티티
 */
@Entity
@Table(name = "landmark")
@Getter
@Setter
@NoArgsConstructor
public class Landmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "landmark_seq")
    private Integer landmarkSeq;

    @Column(name = "landmark_cd", nullable = false, length = 20)
    private String landmarkCd;

    @Column(name = "landmark_nm", nullable = false, length = 100)
    private String landmarkNm;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "sido_cd", length = 20)
    private String sidoCd;

    @Column(name = "gugun_cd", length = 20)
    private String gugunCd;

    @Column(name = "dong_cd", length = 20)
    private String dongCd;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "use_yn", nullable = false, length = 1)
    private String useYn = "Y";

    @Column(name = "sort_ord", nullable = false)
    private Integer sortOrd = 0;

    @Column(name = "reg_id", nullable = false, length = 50)
    private String regId;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;
}