package com.crimescope.service.mapillary;

import com.crimescope.domain.model.District;
import com.crimescope.repository.DistrictRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * Service integrating Mapillary Graph API for verified street-level photographic
 * survey exploration, orientation angles, and geographic incident inspection.
 * Strictly requires an authentic Mapillary access token supplied via environment
 * variables or .env file.
 */
@Service
public class MapillaryService {

    private static final Logger log = LoggerFactory.getLogger(MapillaryService.class);

    private final DistrictRepository districtRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${mapillary.client-token:}")
    private String clientToken;

    @Value("${mapillary.api-url:https://graph.mapillary.com}")
    private String apiUrl;

    @Value("${mapillary.timeout-seconds:15}")
    private int timeoutSeconds;

    public MapillaryService(DistrictRepository districtRepository, ObjectMapper objectMapper) {
        this.districtRepository = districtRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Inspects active Mapillary token configuration, tests connectivity,
     * and returns structured status diagnostics.
     */
    public Map<String, Object> getMapillaryStatus() {
        Map<String, Object> status = new LinkedHashMap<>();

        if (clientToken == null || clientToken.trim().isEmpty() || clientToken.equalsIgnoreCase("your_mapillary_token_here")) {
            status.put("configured", false);
            status.put("valid", false);
            status.put("status", "TOKEN_MISSING");
            status.put("message", "Mapillary client token not configured. Set MAPILLARY_CLIENT_TOKEN in your .env or environment.");
            status.put("tokenMasked", null);
            status.put("setupInstructions", "Obtain a free client token from Mapillary Developer Dashboard (https://www.mapillary.com/dashboard/developers) and set MAPILLARY_CLIENT_TOKEN in .env");
            return status;
        }

        return testTokenConnectivity(clientToken);
    }

    /**
     * Returns configuration metadata for authorized frontend client rendering
     * (e.g. vector tile layers) without exposing backend secrets.
     */
    public Map<String, Object> getMapillaryConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        boolean isConfigured = clientToken != null && !clientToken.trim().isEmpty() && !clientToken.equalsIgnoreCase("your_mapillary_token_here");

        config.put("configured", isConfigured);
        config.put("tokenMasked", isConfigured ? maskToken(clientToken) : null);
        // Mapillary client tokens are client-side public tokens designed for tile layers in browsers
        config.put("clientToken", isConfigured ? clientToken.trim() : null);
        config.put("tileUrlTemplate", isConfigured
                ? "https://tiles.mapillary.com/maps/vtp/mly1_public/2/{z}/{x}/{y}?access_token=" + java.net.URLEncoder.encode(clientToken.trim(), java.nio.charset.StandardCharsets.UTF_8)
                : null);

        return config;
    }

    /**
     * Searches Mapillary Graph API for verified street-level imagery captured
     * near the requested geographic coordinates.
     */
    public Map<String, Object> searchImageryNear(Double lat, Double lng, Integer radiusMeters) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (clientToken == null || clientToken.trim().isEmpty() || clientToken.equalsIgnoreCase("your_mapillary_token_here")) {
            response.put("status", "TOKEN_MISSING");
            response.put("message", "Mapillary client token not configured. Provide MAPILLARY_CLIENT_TOKEN in .env to view authentic street-level photographs.");
            response.put("images", Collections.emptyList());
            return response;
        }

        if (lat == null || lng == null) {
            response.put("status", "INVALID_COORDINATES");
            response.put("message", "Latitude and Longitude parameters are mandatory for street-level exploration.");
            response.put("images", Collections.emptyList());
            return response;
        }

        try {
            // Mapillary Graph API v4 uses closeto=longitude,latitude with OAuth authorization
            String requestUrl = String.format("%s/images?fields=id,computed_geometry,thumb_1024_url,thumb_256_url,captured_at,compass_angle&closeto=%.6f,%.6f&limit=6",
                    apiUrl, lng, lat);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .header("Authorization", "OAuth " + clientToken.trim())
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(httpResponse.body());
                JsonNode dataNode = root.path("data");

                List<Map<String, Object>> imageList = new ArrayList<>();
                if (dataNode.isArray()) {
                    for (JsonNode img : dataNode) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        String id = img.path("id").asText();
                        item.put("id", id);
                        item.put("thumbUrl", img.path("thumb_1024_url").asText(img.path("thumb_256_url").asText("")));
                        item.put("thumbSmallUrl", img.path("thumb_256_url").asText(""));
                        item.put("capturedAt", img.path("captured_at").asText(""));
                        item.put("compassAngle", img.path("compass_angle").asDouble(0.0));

                        JsonNode coords = img.path("computed_geometry").path("coordinates");
                        if (coords.isArray() && coords.size() >= 2) {
                            double imgLng = coords.get(0).asDouble();
                            double imgLat = coords.get(1).asDouble();
                            item.put("longitude", imgLng);
                            item.put("latitude", imgLat);
                            item.put("distanceMeters", Math.round(calculateHaversineDistance(lat, lng, imgLat, imgLng)));
                        }

                        item.put("viewerUrl", "https://www.mapillary.com/app/?pKey=" + id + "&focus=photo");
                        imageList.add(item);
                    }
                }

                // Sort images by proximity to requested coordinates
                imageList.sort(Comparator.comparingDouble(m -> ((Number) m.getOrDefault("distanceMeters", 999999.0)).doubleValue()));

                response.put("status", "SUCCESS");
                response.put("requestedCoordinates", Map.of("latitude", lat, "longitude", lng));
                response.put("count", imageList.size());
                response.put("images", imageList);

                if (imageList.isEmpty()) {
                    response.put("message", String.format("No Mapillary street-level photographic survey found near Lat: %.5f, Lng: %.5f.", lat, lng));
                } else {
                    response.put("message", String.format("Found %d verified street-level surveys near location.", imageList.size()));
                }

                return response;

            } else if (httpResponse.statusCode() == 401 || httpResponse.statusCode() == 403) {
                log.warn("Mapillary authentication failed with HTTP {}", httpResponse.statusCode());
                response.put("status", "TOKEN_INVALID");
                response.put("message", "Mapillary token authorization failed (HTTP " + httpResponse.statusCode() + "). Please check your MAPILLARY_CLIENT_TOKEN.");
                response.put("images", Collections.emptyList());
                return response;
            } else if (httpResponse.statusCode() == 429) {
                log.warn("Mapillary rate limit exceeded.");
                response.put("status", "RATE_LIMITED");
                response.put("message", "Mapillary API rate limit reached. Please wait a moment before querying.");
                response.put("images", Collections.emptyList());
                return response;
            } else {
                response.put("status", "API_ERROR");
                response.put("message", "Mapillary Graph API returned HTTP " + httpResponse.statusCode());
                response.put("images", Collections.emptyList());
                return response;
            }

        } catch (Exception e) {
            log.error("Error communicating with Mapillary Graph API", e);
            response.put("status", "NETWORK_ERROR");
            response.put("message", "Failed to connect to Mapillary service: " + e.getMessage());
            response.put("images", Collections.emptyList());
            return response;
        }
    }

    /**
     * Retrieves survey coverage points for a district administrative headquarters.
     */
    public Map<String, Object> getDistrictStreetCoverage(String districtCode) {
        if (districtCode == null || districtCode.isBlank()) {
            return Map.of("error", "District code is required.");
        }

        District district = districtRepository.findByCodeIgnoreCase(districtCode).orElse(null);
        if (district == null) {
            return Map.of("error", "District not found for code: " + districtCode);
        }

        BigDecimal lat = district.getCenterLat();
        BigDecimal lng = district.getCenterLng();

        if (lat == null || lng == null) {
            return Map.of("error", "No geographic coordinates registered for district: " + district.getName());
        }

        Map<String, Object> result = searchImageryNear(lat.doubleValue(), lng.doubleValue(), 2500);
        result.put("districtCode", district.getCode());
        result.put("districtName", district.getName());
        result.put("headquarters", district.getHeadquarters());
        return result;
    }

    /**
     * Validates a candidate Mapillary token submitted by an operator.
     */
    public Map<String, Object> validateCandidateToken(String tokenToTest) {
        if (tokenToTest == null || tokenToTest.trim().isEmpty()) {
            return Map.of("valid", false, "status", "EMPTY", "message", "Token cannot be empty.");
        }
        return testTokenConnectivity(tokenToTest.trim());
    }

    private Map<String, Object> testTokenConnectivity(String token) {
        Map<String, Object> status = new LinkedHashMap<>();
        try {
            String pingUrl = String.format("%s/images?limit=1", apiUrl);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pingUrl))
                    .header("Authorization", "OAuth " + token.trim())
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                status.put("configured", true);
                status.put("valid", true);
                status.put("status", "OPERATIONAL");
                status.put("message", "Mapillary client token verified. Street-level imagery and vector layers active.");
                status.put("tokenMasked", maskToken(token));
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                status.put("configured", true);
                status.put("valid", false);
                status.put("status", "TOKEN_INVALID");
                status.put("message", "Token authorization failed (HTTP " + response.statusCode() + "). Invalid Mapillary client token.");
                status.put("tokenMasked", maskToken(token));
            } else if (response.statusCode() == 429) {
                status.put("configured", true);
                status.put("valid", false);
                status.put("status", "RATE_LIMITED");
                status.put("message", "Mapillary API rate limit reached (HTTP 429).");
                status.put("tokenMasked", maskToken(token));
            } else {
                status.put("configured", true);
                status.put("valid", false);
                status.put("status", "API_ERROR");
                status.put("message", "Mapillary API returned HTTP " + response.statusCode());
                status.put("tokenMasked", maskToken(token));
            }
        } catch (Exception e) {
            status.put("configured", true);
            status.put("valid", false);
            status.put("status", "NETWORK_ERROR");
            status.put("message", "Failed to reach Mapillary Graph API: " + e.getMessage());
            status.put("tokenMasked", maskToken(token));
        }
        return status;
    }

    private String maskToken(String token) {
        if (token == null || token.length() <= 8) return "********";
        int prefixLen = Math.min(6, token.length() / 3);
        int suffixLen = Math.min(4, token.length() / 4);
        return token.substring(0, prefixLen) + "..." + token.substring(token.length() - suffixLen);
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
