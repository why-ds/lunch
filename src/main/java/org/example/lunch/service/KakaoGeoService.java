package org.example.lunch.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 카카오 REST API - 주소 → 좌표 + 행정동코드 변환
 * 1차: 주소 검색 API
 * 2차: 키워드 검색 API (주소 검색 실패 시)
 * 3차: 좌표 → 행정동 변환 API (시군구 정보 보완)
 */
@Service
public class KakaoGeoService {

    private static final String REST_API_KEY = "5041ce0d2cb4bd0a9a65e1d3b4ede035";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 주소로 좌표 + 행정동코드 검색
     */
    public Map<String, String> getGeoInfo(String address) {
        try {
            // 1차: 주소 검색
            Map<String, String> result = searchByAddress(address);

            // 2차: 주소 검색 실패 시 키워드 검색
            if (result == null) {
                result = searchByKeyword(address);
            }

            // 3차: 좌표는 있는데 시군구 정보가 없으면 좌표→행정동 변환
            if (result != null && (result.get("sido_cd") == null || result.get("sido_cd").isEmpty())) {
                String lat = result.get("latitude");
                String lng = result.get("longitude");
                if (lat != null && lng != null) {
                    Map<String, String> regionInfo = getRegionByCoord(lat, lng);
                    if (regionInfo != null) {
                        result.putAll(regionInfo);
                    }
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("카카오 API 호출 실패: " + e.getMessage());
        }
        return null;
    }

    /**
     * 주소 검색 API
     */
    private Map<String, String> searchByAddress(String address) {
        try {
            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + encoded;

            HttpResponse<String> response = sendRequest(url);
            JsonNode documents = mapper.readTree(response.body()).get("documents");

            if (documents != null && documents.size() > 0) {
                JsonNode first = documents.get(0);
                Map<String, String> result = new HashMap<>();

                result.put("latitude", first.get("y").asText());
                result.put("longitude", first.get("x").asText());

                // address 객체에서 행정동코드 추출
                JsonNode addr = first.get("address");
                if (addr != null && !addr.isNull()) {
                    extractRegionFromAddress(addr, result);
                }

                // address가 없으면 road_address에서 시도 추출
                if ((result.get("sido_cd") == null || result.get("sido_cd").isEmpty())) {
                    JsonNode roadAddr = first.get("road_address");
                    if (roadAddr != null && !roadAddr.isNull()) {
                        result.put("sido_nm", roadAddr.has("region_1depth_name") ? roadAddr.get("region_1depth_name").asText() : "");
                        result.put("gugun_nm", roadAddr.has("region_2depth_name") ? roadAddr.get("region_2depth_name").asText() : "");
                        result.put("dong_nm", roadAddr.has("region_3depth_name") ? roadAddr.get("region_3depth_name").asText() : "");
                    }
                }

                return result;
            }
        } catch (Exception e) {
            System.err.println("주소 검색 실패: " + e.getMessage());
        }
        return null;
    }

    /**
     * 키워드 검색 API
     */
    private Map<String, String> searchByKeyword(String keyword) {
        try {
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + encoded;

            HttpResponse<String> response = sendRequest(url);
            JsonNode documents = mapper.readTree(response.body()).get("documents");

            if (documents != null && documents.size() > 0) {
                JsonNode first = documents.get(0);
                Map<String, String> result = new HashMap<>();
                result.put("latitude", first.get("y").asText());
                result.put("longitude", first.get("x").asText());

                // 키워드 검색에서도 주소 정보 추출
                if (first.has("road_address_name") && !first.get("road_address_name").asText().isEmpty()) {
                    result.put("address", first.get("road_address_name").asText());
                }

                return result;
            }
        } catch (Exception e) {
            System.err.println("키워드 검색 실패: " + e.getMessage());
        }
        return null;
    }

    /**
     * 좌표 → 행정동 변환 API
     * 좌표로 시도/구군/동 코드와 이름을 가져옴
     */
    private Map<String, String> getRegionByCoord(String lat, String lng) {
        try {
            String url = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x=" + lng + "&y=" + lat;

            HttpResponse<String> response = sendRequest(url);
            JsonNode documents = mapper.readTree(response.body()).get("documents");

            if (documents != null && documents.size() > 0) {
                // 행정동 정보 우선 (region_type = H)
                for (JsonNode doc : documents) {
                    if ("H".equals(doc.get("region_type").asText())) {
                        Map<String, String> result = new HashMap<>();
                        String code = doc.get("code").asText();
                        result.put("sido_cd", code.substring(0, 2));
                        result.put("gugun_cd", code.substring(0, 5));
                        result.put("dong_cd", code);
                        result.put("sido_nm", doc.get("region_1depth_name").asText());
                        result.put("gugun_nm", doc.get("region_2depth_name").asText());
                        result.put("dong_nm", doc.get("region_3depth_name").asText());
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("좌표→행정동 변환 실패: " + e.getMessage());
        }
        return null;
    }

    /**
     * address 객체에서 행정동 정보 추출
     */
    private void extractRegionFromAddress(JsonNode addr, Map<String, String> result) {
        if (addr.has("b_code") && !addr.get("b_code").asText().isEmpty()) {
            String bCode = addr.get("b_code").asText();
            result.put("sido_cd", bCode.substring(0, 2));
            result.put("gugun_cd", bCode.substring(0, 5));
            result.put("dong_cd", bCode);
        }
        result.put("sido_nm", addr.has("region_1depth_name") ? addr.get("region_1depth_name").asText() : "");
        result.put("gugun_nm", addr.has("region_2depth_name") ? addr.get("region_2depth_name").asText() : "");
        result.put("dong_nm", addr.has("region_3depth_name") ? addr.get("region_3depth_name").asText() : "");
    }

    /**
     * HTTP 요청 공통
     */
    private HttpResponse<String> sendRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "KakaoAK " + REST_API_KEY)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}