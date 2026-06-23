package com.example.be_foodgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteInfo {

    private double distanceMeters;
    private double distanceKm;
    private double durationSeconds;
    private int durationMinutes;
    private List<double[]> routeCoordinates;

    public static RouteInfo fromMapboxResponse(MapboxDirectionsResponse response, boolean hasCoordinates) {
        if (response == null || response.getRoutes() == null || response.getRoutes().isEmpty()) {
            return null;
        }

        MapboxDirectionsResponse.Route route = response.getRoutes().get(0);
        List<double[]> coords = null;

        if (hasCoordinates && route.getGeometry() != null) {
            coords = decodePolyline(route.getGeometry());
        }

        int durationMinutes = (int) Math.round(route.getDuration() / 60.0);

        return RouteInfo.builder()
                .distanceMeters(route.getDistance())
                .distanceKm(Math.round(route.getDistance() / 1000.0 * 10.0) / 10.0)
                .durationSeconds(route.getDuration())
                .durationMinutes(durationMinutes)
                .routeCoordinates(coords)
                .build();
    }

    public static RouteInfo fromMapboxResponse(MapboxDirectionsResponse response) {
        return fromMapboxResponse(response, false);
    }

    private static List<double[]> decodePolyline(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return List.of();
        }
        int index = 0;
        int len = encoded.length();
        double lat = 0;
        double lng = 0;
        java.util.ArrayList<double[]> coords = new java.util.ArrayList<>();

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            double dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            double dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            coords.add(new double[]{lat / 1e5, lng / 1e5});
        }
        return coords;
    }
}
