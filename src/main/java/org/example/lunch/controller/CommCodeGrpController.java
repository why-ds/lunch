package org.example.lunch.controller;

import org.example.lunch.entity.CommCodeGrp;
import org.example.lunch.entity.CommCodeDtl;
import org.example.lunch.repository.CommCodeGrpRepository;
import org.example.lunch.repository.CommCodeDtlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CommCodeGrpController {

    private final CommCodeGrpRepository commCodeGrpRepository;
    private final CommCodeDtlRepository commCodeDtlRepository;

    @GetMapping("/groups")
    public List<CommCodeGrp> getGroups() {
        return commCodeGrpRepository.findAll();
    }

    /**
     * 공통코드 상세 목록 조회
     * GET /api/codes/details?grpCd={그룹코드}
     * 사용 예: 호선 목록(SUBWAY_LINE) 또는 특정 호선의 역 목록 조회
     */
    @GetMapping("/details")
    public List<CommCodeDtl> getCodeDetails(@RequestParam String grpCd) {
        // null 체크 처리: grpCd가 null이거나 비어있으면 빈 리스트 반환하여 500 에러 방지
        if (grpCd == null || grpCd.trim().isEmpty()) {
            return List.of();
        }
        return commCodeDtlRepository.findByGrpCdAndUseYnOrderByDtlCdAsc(grpCd, "Y");
    }
}