package org.example.lunch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공통코드 상세 엔티티
 * - 테이블명: comm_code_dtl
 */
@Entity
@Table(name = "comm_code_dtl")
@Getter
@NoArgsConstructor
public class CommCodeDtl {

    /** 상세코드 일련번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dtl_seq")
    private Integer dtlSeq;

    /** 그룹코드 */
    @Column(name = "grp_cd", nullable = false, length = 20)
    private String grpCd;

    /** 상세코드 */
    @Column(name = "dtl_cd", nullable = false, length = 20)
    private String dtlCd;

    /** 상세코드명 */
    @Column(name = "dtl_nm", nullable = false, length = 100)
    private String dtlNm;

    /** 사용여부 (Y/N) */
    @Column(name = "use_yn", nullable = false, length = 1)
    private String useYn;

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