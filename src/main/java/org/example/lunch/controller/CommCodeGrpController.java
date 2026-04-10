package org.example.lunch.controller;

import org.example.lunch.entity.CommCodeGrp;
import org.example.lunch.repository.CommCodeGrpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 공통코드 그룹 API 컨트롤러
 * - 일단 조회 하나만 딱!
 */
@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CommCodeGrpController {

    /** Repository 주입 (생성자 주입 - @RequiredArgsConstructor) */
    private final CommCodeGrpRepository commCodeGrpRepository;

    /**
     * 공통코드 그룹 전체 목록 조회
     * GET /api/codes/groups
     *
     * @return 공통코드 그룹 리스트 (JSON)
     */
    @GetMapping("/groups")
    public List<CommCodeGrp> getGroups() {
        return commCodeGrpRepository.findAll();
    }
}