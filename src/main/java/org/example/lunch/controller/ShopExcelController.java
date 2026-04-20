package org.example.lunch.controller;

import org.example.lunch.entity.Shop;
import org.example.lunch.entity.SubwayStation;
import org.example.lunch.entity.CommCodeDtl;
import org.example.lunch.repository.ShopRepository;
import org.example.lunch.repository.SubwayStationRepository;
import org.example.lunch.repository.CommCodeDtlRepository;
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
 * 가게 엑셀 업로드 API
 *
 * 엑셀 컬럼 순서:
 * 0: 가게명 (필수)
 * 1: 호선 (텍스트: 1호선 / 코드도 가능)
 * 2: 역명 (텍스트: 시청 / 코드도 가능: ST0132)
 * 3: 음식종류 (텍스트: 한식 / 코드도 가능: F01)
 * 4: 주소
 * 5: 비고
 */
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopExcelController {

    private final ShopRepository shopRepository;
    private final KakaoGeoService kakaoGeoService;
    private final SubwayStationRepository subwayStationRepository;
    private final CommCodeDtlRepository commCodeDtlRepository;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<Shop> shopList = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // 0: 가게명 (필수)
                    String shopNm = getCellValue(row.getCell(0));
                    if (shopNm == null || shopNm.trim().isEmpty()) continue;
                    if (shopNm.startsWith("※")) continue;

                    // 3: 음식종류 (필수)
                    String foodTypeInput = getCellValue(row.getCell(3));
                    if (foodTypeInput == null || foodTypeInput.trim().isEmpty()) continue;

                    Shop shop = new Shop();
                    shop.setShopNm(shopNm.trim());

                    // 1,2: 호선 + 역명 → 역코드 변환
                    String lineInput = getCellValue(row.getCell(1));
                    String stationInput = getCellValue(row.getCell(2));
                    if (lineInput != null && stationInput != null
                            && !lineInput.isEmpty() && !stationInput.isEmpty()) {
                        if (stationInput.startsWith("ST")) {
                            // 코드로 직접 입력
                            shop.setStationCd(stationInput);
                        } else {
                            // 텍스트로 입력 (호선 + 역명)
                            String stationNmClean = stationInput.trim().replaceAll("역$", "");

                            // 정확히 매칭 시도
                            SubwayStation station = subwayStationRepository
                                    .findByLineNmAndStationNm(lineInput.trim(), stationInput.trim());

                            // 못 찾으면 "역" 떼고 재시도
                            if (station == null) {
                                station = subwayStationRepository
                                        .findByLineNmAndStationNm(lineInput.trim(), stationNmClean);
                            }

                            // 그래도 못 찾으면 "역" 붙여서 재시도
                            if (station == null) {
                                station = subwayStationRepository
                                        .findByLineNmAndStationNm(lineInput.trim(), stationNmClean + "역");
                            }

                            if (station != null) {
                                shop.setStationCd(station.getStationCd());
                            } else {
                                errors.add((i + 1) + "행: 역을 찾을 수 없음 - " + lineInput + " " + stationInput);
                            }
                        }
                    }

                    // 3: 음식종류 코드 변환
                    if (foodTypeInput.startsWith("F")) {
                        shop.setFoodTypeCd(foodTypeInput);
                    } else {
                        CommCodeDtl foodCode = commCodeDtlRepository
                                .findByGrpCdAndDtlNm("FOOD_TYPE", foodTypeInput.trim());
                        if (foodCode != null) {
                            shop.setFoodTypeCd(foodCode.getDtlCd());
                        } else {
                            failCount++;
                            errors.add((i + 1) + "행: 음식종류를 찾을 수 없음 - " + foodTypeInput);
                            continue;
                        }
                    }

                    // 4: 주소 → 카카오 API로 좌표 + 시군구 자동 변환
                    String address = getCellValue(row.getCell(4));
                    shop.setAddress(address);

                    if (address != null && !address.isEmpty()) {
                        Map<String, String> geoInfo = kakaoGeoService.getGeoInfo(address);
                        if (geoInfo != null) {
                            if (geoInfo.get("latitude") != null) {
                                shop.setLatitude(Double.parseDouble(geoInfo.get("latitude")));
                            }
                            if (geoInfo.get("longitude") != null) {
                                shop.setLongitude(Double.parseDouble(geoInfo.get("longitude")));
                            }
                            shop.setSidoCd(geoInfo.getOrDefault("sido_cd", ""));
                            shop.setGugunCd(geoInfo.getOrDefault("gugun_cd", ""));
                            shop.setDongCd(geoInfo.getOrDefault("dong_cd", ""));
                            shop.setSidoNm(geoInfo.getOrDefault("sido_nm", ""));
                            shop.setGugunNm(geoInfo.getOrDefault("gugun_nm", ""));
                            shop.setDongNm(geoInfo.getOrDefault("dong_nm", ""));
                        }
                    }

                    // 5: 비고
                    shop.setRmk(getCellValue(row.getCell(5)));
                    shop.setCloseYn("N");
                    shop.setRegId("EXCEL_UPLOAD");
                    shop.setRegDt(java.time.LocalDateTime.now());

                    // 중복 체크 (가게명 + 주소)
                    if (shopRepository.existsByShopNmAndAddress(shopNm.trim(), address)) {
                        failCount++;
                        errors.add((i + 1) + "행: 이미 등록된 가게 - " + shopNm);
                        continue;
                    }

                    shopList.add(shop);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.add((i + 1) + "행: " + e.getMessage());
                }
            }

            shopRepository.saveAll(shopList);

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
}