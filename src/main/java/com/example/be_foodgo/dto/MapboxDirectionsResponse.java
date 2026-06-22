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
public class MapboxDirectionsResponse {

    private String code;
    private List<Route> routes;
    private String uuid;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Route {
        private String geometry;
        private List<Leg> legs;
        private double distance;
        private double duration;
        private String weightName;
        private double weight;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Leg {
        private List<Step> steps;
        private double distance;
        private double duration;
        private String summary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Step {
        private double distance;
        private double duration;
        private String geometry;
        private String instruction;
        private List<Double> location;
    }
}
