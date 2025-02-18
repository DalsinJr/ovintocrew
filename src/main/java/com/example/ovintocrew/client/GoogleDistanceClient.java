package com.example.ovintocrew.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;


@Component
public class GoogleDistanceClient {

    private final String apiKey;
    private final String baseUrl;
    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleDistanceClient(
            @Value("${google.client.api-key}") String apiKey,
            @Value("${google.client.base-url}") String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public double getDistance(double originLat, double originLon, double destLat, double destLon) throws Exception {
        String url = String.format("%s?origins=%f,%f&destinations=%f,%f&key=%s",
                baseUrl, originLat, originLon, destLat, destLon, apiKey);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Google API responded with status: " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        if (!"OK".equals(root.get("status").asText())) {
            throw new Exception("Invalid response from Google Distance Matrix API");
        }
        JsonNode rows = root.get("rows");
        if (rows.isArray() && !rows.isEmpty()) {
            JsonNode elements = rows.get(0).get("elements");
            if (elements.isArray() && !elements.isEmpty()) {
                JsonNode element = elements.get(0);
                if ("OK".equals(element.get("status").asText())) {
                    int distanceMeters = element.get("distance").get("value").asInt();
                    return distanceMeters / 1000.0;
                }
            }
        }
        throw new Exception("Invalid response from Google Distance Matrix API");
    }
}


