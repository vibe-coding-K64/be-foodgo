package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.MapboxDirectionsResponse;
import com.example.be_foodgo.dto.RouteInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class MapboxService {

    private static final Logger log = LoggerFactory.getLogger(MapboxService.class);
    private static final String BASE_URL = "https://api.mapbox.com/directions/v5/mapbox";

    private final WebClient webClient;
    private final String accessToken;
    private final boolean isEnabled;

    public MapboxService(
            WebClient.Builder webClientBuilder,
            @Value("${mapbox.access-token:}") String accessToken) {
        this.accessToken = accessToken;
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.isEnabled = accessToken != null
                && !accessToken.isBlank()
                && !accessToken.equals("YOUR_MAPBOX_ACCESS_TOKEN_HERE");
        if (isEnabled) {
            log.info("Mapbox service initialized with valid access token");
        } else {
            log.warn("Mapbox access token not configured. Distance calculations will fall back to Haversine formula.");
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public RouteInfo getRouteInfo(double fromLat, double fromLng,
                                  double toLat, double toLng,
                                  String profile) {
        return getRouteInfo(fromLat, fromLng, toLat, toLng, profile, false);
    }

    public RouteInfo getRouteInfo(double fromLat, double fromLng,
                                  double toLat, double toLng,
                                  String profile, boolean includeCoordinates) {
        if (!isEnabled) {
            log.debug("Mapbox disabled, falling back to Haversine for ({},{}) -> ({},{})",
                    fromLat, fromLng, toLat, toLng);
            return null;
        }

        String coordinates = fromLng + "," + fromLat + ";" + toLng + "," + toLat;
        String geometries = includeCoordinates ? "polyline" : "polyline6";

        try {
            MapboxDirectionsResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{profile}/{coordinates}")
                            .queryParam("access_token", accessToken)
                            .queryParam("geometries", geometries)
                            .queryParam("overview", includeCoordinates ? "full" : "false")
                            .queryParam("steps", false)
                            .build(profile, coordinates))
                    .retrieve()
                    .bodyToMono(MapboxDirectionsResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorResume(e -> {
                        log.warn("Mapbox API call failed, falling back to Haversine: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            return RouteInfo.fromMapboxResponse(response, includeCoordinates);
        } catch (Exception e) {
            log.warn("Mapbox route lookup failed for ({},{}) -> ({},{}): {}",
                    fromLat, fromLng, toLat, toLng, e.getMessage());
            return null;
        }
    }

    public double getRoadDistanceKm(double fromLat, double fromLng,
                                     double toLat, double toLng) {
        return getRoadDistanceKm(fromLat, fromLng, toLat, toLng, "driving");
    }

    public double getRoadDistanceKm(double fromLat, double fromLng,
                                     double toLat, double toLng,
                                     String profile) {
        RouteInfo route = getRouteInfo(fromLat, fromLng, toLat, toLng, profile);
        if (route != null) {
            return route.getDistanceKm();
        }
        return calculateHaversineKm(fromLat, fromLng, toLat, toLng);
    }

    public int getEstimatedDurationMinutes(double fromLat, double fromLng,
                                           double toLat, double toLng) {
        return getEstimatedDurationMinutes(fromLat, fromLng, toLat, toLng, "driving");
    }

    public int getEstimatedDurationMinutes(double fromLat, double fromLng,
                                           double toLat, double toLng,
                                           String profile) {
        RouteInfo route = getRouteInfo(fromLat, fromLng, toLat, toLng, profile);
        if (route != null) {
            return route.getDurationMinutes();
        }
        int km = (int) Math.round(calculateHaversineKm(fromLat, fromLng, toLat, toLng));
        return Math.max(5, km * 4);
    }

    public RouteInfo getStoreToDeliveryRoute(double storeLat, double storeLng,
                                             double deliveryLat, double deliveryLng) {
        return getRouteInfo(storeLat, storeLng, deliveryLat, deliveryLng, "driving", false);
    }

    public RouteInfo getDriverToStoreRoute(double driverLat, double driverLng,
                                           double storeLat, double storeLng) {
        return getRouteInfo(driverLat, driverLng, storeLat, storeLng, "driving", false);
    }

    public double getDriverToStoreDistanceKm(double driverLat, double driverLng,
                                              double storeLat, double storeLng) {
        RouteInfo route = getRouteInfo(driverLat, driverLng, storeLat, storeLng, "driving");
        if (route != null) {
            return route.getDistanceKm();
        }
        return calculateHaversineKm(driverLat, driverLng, storeLat, storeLng);
    }

    private double calculateHaversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(earthRadiusKm * c * 10.0) / 10.0;
    }
}
