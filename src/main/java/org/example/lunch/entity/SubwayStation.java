package org.example.lunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subway_station") // 🚨 테이블명 언더바(_) 추가!
@Getter
@NoArgsConstructor
public class SubwayStation {

    /** 일련번호 (진짜 PK) */
    @Id
    @Column(name = "station_seq", nullable = false)
    private Integer stationSeq;

    /** 지하철역 코드 */
    @Column(name = "station_cd", length = 20, nullable = false)
    private String stationCd;

    /** 지하철 역명 (예: 강남, 역삼) */
    @Column(name = "station_nm", length = 100, nullable = false)
    private String stationNm;

    /** 호선명 (예: 1호선, 2호선) */
    @Column(name = "line_nm", length = 50, nullable = false)
    private String lineNm;
}