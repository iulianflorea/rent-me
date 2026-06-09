package com.singularity.rentit.service;

import com.singularity.rentit.dto.response.GeoSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeoService {

    private final RestClient restClient;

    public GeoService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader("User-Agent", "RentIt/1.0")
                .build();
    }

    @SuppressWarnings("unchecked")
    public List<GeoSearchResponse> search(String query) {
        try {
            List<Map<String, Object>> results = restClient.get()
                    .uri("/search?q={query}&format=json&addressdetails=1&limit=5&countrycodes=ro", query)
                    .retrieve()
                    .body(List.class);

            if (results == null) return List.of();

            return results.stream()
                    .map(r -> {
                        Map<String, Object> address = (Map<String, Object>) r.get("address");
                        String city = extractCity(address);
                        String county = extractCounty(address);
                        return new GeoSearchResponse(
                                (String) r.get("display_name"),
                                Double.parseDouble((String) r.get("lat")),
                                Double.parseDouble((String) r.get("lon")),
                                city,
                                county,
                                "Romania"
                        );
                    })
                    .toList();
        } catch (Exception e) {
            log.warn("Nominatim search failed for query '{}': {}", query, e.getMessage());
            return List.of();
        }
    }

    public double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @SuppressWarnings("unchecked")
    private String extractCity(Map<String, Object> address) {
        if (address == null) return null;
        for (String key : Arrays.asList("city", "town", "village", "municipality")) {
            if (address.containsKey(key)) return (String) address.get(key);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractCounty(Map<String, Object> address) {
        if (address == null) return null;
        Object county = address.get("county");
        if (county != null) return county.toString().replace(" County", "").replace(" Județ", "");
        Object state = address.get("state");
        return state != null ? state.toString() : null;
    }
}
