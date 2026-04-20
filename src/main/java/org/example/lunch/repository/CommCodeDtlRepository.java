package org.example.lunch.repository;

import org.example.lunch.entity.CommCodeDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommCodeDtlRepository extends JpaRepository<CommCodeDtl, Integer> {
    /**
     * 특정 그룹코드(grpCd)에 속하면서 사용여부(useYn)가 'Y'인 상세 코드만 오름차순으로 조회합니다.
     * 프론트엔드 셀렉트 박스 렌더링에 사용됩니다.
     */
    List<CommCodeDtl> findByGrpCdAndUseYnOrderByDtlCdAsc(String grpCd, String useYn);
    CommCodeDtl findByGrpCdAndDtlNm(String grpCd, String dtlNm);
}