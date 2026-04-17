package org.example.lunch.controller;

import org.example.lunch.entity.SubwayStation;
import org.example.lunch.repository.SubwayStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subway-stations")
@RequiredArgsConstructor
public class SubwayStationController {

    private final SubwayStationRepository subwayStationRepository;

    /** 식당이 있는 호선 목록 조회 */
    @GetMapping("/lines")
    public List<String> getLines() {
        return subwayStationRepository.findLinesWithShops();
    }

    /** 해당 호선 내 식당이 있는 역 목록 조회 */
    @GetMapping
    public List<SubwayStation> getStationsByLine(@RequestParam String lineNm) {
        if (lineNm == null || lineNm.trim().isEmpty()) {
            return List.of();
        }
        return subwayStationRepository.findStationsByLineWithShops(lineNm.trim());
    }
}