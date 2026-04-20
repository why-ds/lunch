package org.example.lunch.controller;

import org.example.lunch.entity.Landmark;
import org.example.lunch.repository.LandmarkRepository;
import org.example.lunch.service.KakaoGeoService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

/**
 * 랜드마크 API
 * - 엑셀 업로드 + 목록 조회
 */
@RestController
@RequestMapping("/api/landmarks")
@RequiredArgsConstructor
public class LandmarkController {

    private final LandmarkRepository landmarkRepository;
    private final KakaoGeoService kakaoGeoService;

    /**
     * 랜드마크 목록 조회
     * GET /api/landmarks
     */
    @GetMapping
    public List<Landmark> getLandmarks() {
        return landmarkRepository.findByUseYnOrderBySortOrd("Y");
    }

    /**
     * 랜드마크 엑셀 업로드
     * POST /api/landmarks/upload
     *
     * 엑셀 컬럼:
     * 0: 랜드마크코드 (필수)
     * 1: 랜드마크명 (필수)
     * 2: 주소 (주소 넣으면 좌표+시군구코드 자동변환)
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<Landmark> landmarkList = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String landmarkCd = getCellValue(row.getCell(0));
                    if (landmarkCd == null || landmarkCd.trim().isEmpty()) continue;
                    if (landmarkCd.startsWith("※")) continue;

                    String landmarkNm = getCellValue(row.getCell(0));
                    if (landmarkNm == null || landmarkNm.trim().isEmpty()) continue;
                    if (landmarkNm.startsWith("※")) continue;

                    String address = getCellValue(row.getCell(1));

                    Landmark landmark = new Landmark();
                    landmark.setLandmarkNm(landmarkNm.trim());

                    // 주소로 카카오 API 호출
                    //String address = getCellValue(row.getCell(2));
                    landmark.setAddress(address);

                    if (address != null && !address.isEmpty()) {
                        Map<String, String> geoInfo = kakaoGeoService.getGeoInfo(address);
                        if (geoInfo != null) {
                            if (geoInfo.get("latitude") != null) {
                                landmark.setLatitude(Double.parseDouble(geoInfo.get("latitude")));
                            }
                            if (geoInfo.get("longitude") != null) {
                                landmark.setLongitude(Double.parseDouble(geoInfo.get("longitude")));
                            }
                            landmark.setSidoCd(geoInfo.getOrDefault("sido_cd", ""));
                            landmark.setGugunCd(geoInfo.getOrDefault("gugun_cd", ""));
                            landmark.setDongCd(geoInfo.getOrDefault("dong_cd", ""));
                            landmark.setSidoNm(geoInfo.getOrDefault("sido_nm", ""));
                            landmark.setGugunNm(geoInfo.getOrDefault("gugun_nm", ""));
                            landmark.setDongNm(geoInfo.getOrDefault("dong_nm", ""));
                        }
                    }

                    landmark.setUseYn("Y");
                    landmark.setSortOrd(i);
                    landmark.setRegId("EXCEL_UPLOAD");
                    landmark.setRegDt(java.time.LocalDateTime.now());

                    // 중복 체크 (이름 + 주소)
                    if (landmarkRepository.existsByLandmarkNmAndAddress(landmarkNm.trim(), address)) {
                        failCount++;
                        errors.add((i + 1) + "행: 이미 등록된 랜드마크 - " + landmarkNm);
                        continue;
                    }

                    /*Landmark landmark = new Landmark();*/
                    landmark.setLandmarkNm(landmarkNm.trim());
                    landmark.setAddress(address);

                    landmarkList.add(landmark);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.add((i + 1) + "행: " + e.getMessage());
                }
            }

            landmarkRepository.saveAll(landmarkList);

            result.put("success", true);
            result.put("message", successCount + "건 저장 완료");
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            if (!errors.isEmpty()) {
                result.put("errors", errors);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "업로드 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BLANK:
                return null;
            default:
                return cell.toString().trim();
        }
    }
    /**
     * 랜드마크 단건 등록
     * POST /api/landmarks/single
     */
    @PostMapping("/single")
    public ResponseEntity<Map<String, Object>> createLandmark(@RequestBody Landmark landmark) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (landmark.getAddress() != null && !landmark.getAddress().isEmpty()) {
                Map<String, String> geoInfo = kakaoGeoService.getGeoInfo(landmark.getAddress());
                if (geoInfo != null) {
                    if (geoInfo.get("latitude") != null) landmark.setLatitude(Double.parseDouble(geoInfo.get("latitude")));
                    if (geoInfo.get("longitude") != null) landmark.setLongitude(Double.parseDouble(geoInfo.get("longitude")));
                    landmark.setSidoCd(geoInfo.getOrDefault("sido_cd", ""));
                    landmark.setGugunCd(geoInfo.getOrDefault("gugun_cd", ""));
                    landmark.setDongCd(geoInfo.getOrDefault("dong_cd", ""));
                    landmark.setSidoNm(geoInfo.getOrDefault("sido_nm", ""));
                    landmark.setGugunNm(geoInfo.getOrDefault("gugun_nm", ""));
                    landmark.setDongNm(geoInfo.getOrDefault("dong_nm", ""));
                }
            }
            landmark.setUseYn("Y");
            landmark.setSortOrd(0);
            landmark.setRegId("ADMIN");
            landmark.setRegDt(java.time.LocalDateTime.now());
            landmarkRepository.save(landmark);
            result.put("success", true);
            result.put("message", "등록 완료: " + landmark.getLandmarkNm());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "등록 실패: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    /**
     * 랜드마크가 존재하는 시도 목록
     * GET /api/landmarks/sidos
     */
    @GetMapping("/sidos")
    public List<String> getSidos() {
        return landmarkRepository.findDistinctSidoNms();
    }

    /**
     * 특정 시도 내 랜드마크가 존재하는 시군구 목록
     * GET /api/landmarks/guguns?sidoCd=11
     */
    @GetMapping("/guguns")
    public List<Map<String, String>> getGuguns(@RequestParam String sidoNm) {
        List<Object[]> list = landmarkRepository.findDistinctGugunsBySidoNm(sidoNm);
        List<Map<String, String>> result = new java.util.ArrayList<>();
        for (Object[] row : list) {
            Map<String, String> map = new java.util.HashMap<>();
            map.put("gugunCd", (String) row[0]);
            map.put("gugunNm", (String) row[1]);
            result.add(map);
        }
        return result;
    }


    /**
     * 특정 시군구 내 랜드마크 목록
     * GET /api/landmarks/filter?gugunCd=11140
     */
    @GetMapping("/filter")
    public List<Landmark> getByGugun(@RequestParam String gugunCd) {
        return landmarkRepository.findByGugunCdAndUseYnOrderBySortOrd(gugunCd, "Y");
    }
}