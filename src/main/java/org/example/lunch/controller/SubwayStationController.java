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

    @GetMapping("/lines")
    public List<String> getLines() {
        return subwayStationRepository.findAllDistinctLines();
    }

    @GetMapping
    public List<SubwayStation> getStationsByLine(@RequestParam String lineNm) {
        if (lineNm == null || lineNm.trim().isEmpty()) return List.of();
        return subwayStationRepository.findByLineNmOrderByStationNmAsc(lineNm.trim());
    }
}