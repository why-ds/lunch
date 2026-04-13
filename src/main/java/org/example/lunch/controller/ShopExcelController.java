package org.example.lunch.controller;

import org.example.lunch.entity.CommCodeGrp;
import org.example.lunch.repository.CommCodeGrpRepository;
import org.example.lunch.entity.Shop;
import org.example.lunch.repository.ShopRepository;
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
 * - 엑셀 파일을 받아서 shop 테이블에 일괄 저장
 */
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopExcelController {

    private final ShopRepository shopRepository;

    /**
     * 엑셀 파일 업로드 → shop 테이블에 일괄 저장
     * POST /api/shops/upload
     *
     * @param file 업로드할 엑셀 파일 (.xlsx)
     * @return 저장된 가게 수
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            // 첫 번째 시트 읽기
            Sheet sheet = workbook.getSheetAt(0);
            List<Shop> shopList = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();

            // 2번째 행부터 데이터 읽기 (1번째 행은 헤더)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // 가게명 필수 체크
                    String shopNm = getCellValue(row.getCell(0));
                    if (shopNm == null || shopNm.trim().isEmpty()) continue;

                    // Shop 엔티티 생성
                    Shop shop = new Shop();
                    shop.setShopNm(shopNm.trim());
                    shop.setAreaCd(getCellValue(row.getCell(1)));
                    shop.setFoodTypeCd(getCellValue(row.getCell(2)));
                    shop.setBizHourCd(getCellValue(row.getCell(3)));
                    shop.setWalkDistCd(getCellValue(row.getCell(4)));
                    shop.setRmk(getCellValue(row.getCell(5)));
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

            // 일괄 저장
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

    /**
     * 셀 값을 문자열로 변환
     * - null, 숫자, 문자열 처리
     */
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