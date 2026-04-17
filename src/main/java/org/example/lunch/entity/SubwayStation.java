package org.example.lunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지하철역 엔티티
 * - 테이블명: subwaystation
 */
@Entity
@Table(name = "subwaystation")
@Getter
@NoArgsConstructor
public class SubwayStation {

    /** 지하철역 코드 (PK) */
    @Id
    @Column(name = "station_cd", length = 20, nullable = false)
    private String stationCd;

    /** 지하철 역명 (예: 강남, 역삼) */
    @Column(name = "station_nm", length = 100, nullable = false)
    private String stationNm;

    /** 호선명 (예: 1호선, 2호선) */
    @Column(name = "line_nm", length = 50, nullable = false)
    private String lineNm;
}