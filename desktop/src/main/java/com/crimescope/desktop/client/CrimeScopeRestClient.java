package com.crimescope.desktop.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;

/**
 * High-performance REST API Client using Java 21 native HttpClient.
 * Ensures consistent business rule enforcement between Web and Desktop interfaces.
 */
public class CrimeScopeRestClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String jwtToken = null;

    public CrimeScopeRestClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public boolean login(String username, String password) throws IOException, InterruptedException {
        String url = baseUrl + "/api/auth/login";
        Map<String, String> body = Map.of("username", username, "password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonNode node = objectMapper.readTree(response.body());
            this.jwtToken = node.path("token").asText();
            return true;
        }
        return false;
    }

    public JsonNode getDistricts() throws IOException, InterruptedException {
        return getJson("/api/districts");
    }

    public JsonNode getCategories() throws IOException, InterruptedException {
        return getJson("/api/crime-categories");
    }

    public JsonNode getCrimeRecords(Long districtId, Long categoryId, Integer year, int page, int size)
            throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder("/api/crime-records?page=").append(page).append("&size=").append(size);
        if (districtId != null) sb.append("&districtId=").append(districtId);
        if (categoryId != null) sb.append("&categoryId=").append(categoryId);
        if (year != null) sb.append("&startYear=").append(year).append("&endYear=").append(year);
        sb.append("&includeDemo=true");

        return getJson(sb.toString());
    }

    public JsonNode createCrimeRecord(Map<String, Object> recordDto) throws IOException, InterruptedException {
        String url = baseUrl + "/api/crime-records";
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(recordDto)));

        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    public boolean deleteCrimeRecord(Long recordId) throws IOException, InterruptedException {
        String url = baseUrl + "/api/crime-records/" + recordId;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE();

        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 204 || response.statusCode() == 200;
    }

    public JsonNode importCsvFile(File file) throws IOException, InterruptedException {
        String boundary = "---CrimeScopeBoundary" + System.currentTimeMillis();
        String url = baseUrl + "/api/data/import/csv";

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        StringBuilder header = new StringBuilder();
        header.append("--").append(boundary).append("\r\n");
        header.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
        header.append("Content-Type: text/csv\r\n\r\n");

        byte[] headerBytes = header.toString().getBytes();
        byte[] footerBytes = ("\r\n--" + boundary + "--\r\n").getBytes();

        byte[] multipartPayload = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
        System.arraycopy(headerBytes, 0, multipartPayload, 0, headerBytes.length);
        System.arraycopy(fileBytes, 0, multipartPayload, headerBytes.length, fileBytes.length);
        System.arraycopy(footerBytes, 0, multipartPayload, headerBytes.length + fileBytes.length, footerBytes.length);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartPayload));

        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    public JsonNode runJdbcDemo(String districtCode, Integer year) throws IOException, InterruptedException {
        String url = baseUrl + "/api/system/jdbc-demo?districtCode=" + (districtCode != null ? districtCode : "CHN") +
                (year != null ? "&year=" + year : "");
        return getJson(url.replace(baseUrl, ""));
    }

    public String downloadTemplate() throws IOException, InterruptedException {
        String url = baseUrl + "/api/data/template";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private JsonNode getJson(String endpoint) throws IOException, InterruptedException {
        String url = baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    public boolean isAuthenticated() {
        return jwtToken != null;
    }
}
