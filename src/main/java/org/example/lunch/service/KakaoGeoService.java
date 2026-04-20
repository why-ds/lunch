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
 */
@Service
public class KakaoGeoService {

    private static final String REST_API_KEY = "5041ce0d2cb4bd0a9a65e1d3b4ede035";

    /**
     * 주소로 좌표 + 행정동코드 검색
     * @param address 검색할 주소
     * @return {latitude, longitude, sido_cd, gugun_cd, dong_cd} 또는 null
     */
    public Map<String, String> getGeoInfo(String address) {
        try {
            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + encoded;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "KakaoAK " + REST_API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            JsonNode documents = root.get("documents");

            if (documents != null && documents.size() > 0) {
                JsonNode first = documents.get(0);
                Map<String, String> result = new HashMap<>();

                // 좌표
                result.put("latitude", first.get("y").asText());
                result.put("longitude", first.get("x").asText());

                // 행정동 코드
                JsonNode roadAddr = first.get("road_address");
                JsonNode addr = first.get("address");

                if (addr != null) {
                    result.put("sido_cd", addr.has("b_code") ? addr.get("b_code").asText().substring(0, 2) : "");
                    result.put("gugun_cd", addr.has("b_code") ? addr.get("b_code").asText().substring(0, 5) : "");
                    result.put("dong_cd", addr.has("b_code") ? addr.get("b_code").asText() : "");

                    // 시도명, 구군명, 동명 추가
                    result.put("sido_nm", addr.has("region_1depth_name") ? addr.get("region_1depth_name").asText() : "");
                    result.put("gugun_nm", addr.has("region_2depth_name") ? addr.get("region_2depth_name").asText() : "");
                    result.put("dong_nm", addr.has("region_3depth_name") ? addr.get("region_3depth_name").asText() : "");
                }

                return result;
            }

            // 주소 검색 안 되면 키워드 검색 시도
            return getGeoInfoByKeyword(address);

        } catch (Exception e) {
            System.err.println("카카오 API 호출 실패: " + e.getMessage());
        }
        return null;
    }

    /**
     * 키워드로 검색 (주소 검색 실패 시 대체)
     */
    private Map<String, String> getGeoInfoByKeyword(String keyword) {
        try {
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + encoded;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "KakaoAK " + REST_API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            JsonNode documents = root.get("documents");

            if (documents != null && documents.size() > 0) {
                JsonNode first = documents.get(0);
                Map<String, String> result = new HashMap<>();
                result.put("latitude", first.get("y").asText());
                result.put("longitude", first.get("x").asText());
                return result;
            }
        } catch (Exception e) {
            System.err.println("카카오 키워드 검색 실패: " + e.getMessage());
        }
        return null;
    }
}