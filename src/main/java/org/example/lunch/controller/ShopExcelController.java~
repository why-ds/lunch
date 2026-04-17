package org.example.lunch.controller;

import org.example.lunch.entity.Shop;
import org.example.lunch.repository.ShopRepository;
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
 */
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopExcelController {

    private final ShopRepository shopRepository;
    private final KakaoGeoService kakaoGeoService;

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
                    String shopNm = getCellValue(row.getCell(0));
                    if (shopNm == null || shopNm.trim().isEmpty()) continue;

                    Shop shop = new Shop();
                    shop.setShopNm(shopNm.trim());
                    shop.setStationCd(getCellValue(row.getCell(1)));
                    shop.setFoodTypeCd(getCellValue(row.getCell(2)));

                    String address = getCellValue(row.getCell(3));
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
                        }
                    }

                    shop.setRmk(getCellValue(row.getCell(4)));
                    shop.setCloseYn("N");
                    shop.setRegId("EXCEL_UPLOAD");
                    shop.setRegDt(java.time.LocalDateTime.now());

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