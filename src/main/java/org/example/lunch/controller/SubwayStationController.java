package org.example.lunch.controller;

import org.example.lunch.entity.SubwayStation;
import org.example.lunch.repository.SubwayStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 지하철역 API
 */
@RestController
@RequestMapping("/api/subway-stations")
@RequiredArgsConstructor
public class SubwayStationController {

    private final SubwayStationRepository subwayStationRepository;

    /**
     * 식당이 존재하는 호선 목록 조회
     * GET /api/subway-stations/lines
     */
    @GetMapping("/lines")
    public List<String> getLines() {
        return subwayStationRepository.findLinesWithShops();
    }

    /**
     * 특정 호선의 역 목록 조회 (식당이 있는 역만)
     * GET /api/subway-stations?lineNm=1호선
     */
    @GetMapping
    public List<SubwayStation> getStations(@RequestParam String lineNm) {
        return subwayStationRepository.findStationsByLineWithShops(lineNm);
    }
}