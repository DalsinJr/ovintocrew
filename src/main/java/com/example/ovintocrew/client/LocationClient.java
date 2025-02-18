package com.example.ovintocrew.client;

import com.example.ovintocrew.model.dto.LocationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class LocationClient {

    private final String baseUrl;
    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public LocationClient(@Value("${location.client.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public LocationResponse getLocation(String employeeId) {
        String url = baseUrl + employeeId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Failed to fetch location for employee "
                        + employeeId + ". HTTP status: " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), LocationResponse.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Exception fetching location for employee "
                    + employeeId, ex);
        }
    }
}
