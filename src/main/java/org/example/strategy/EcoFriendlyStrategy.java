package org.example.strategy;

import org.example.model.*;
import java.util.ArrayList;
import java.util.List;



    public class EcoFriendlyStrategy implements RouteStrategy {

        @Override
        public RouteResult calculate(Graph graph, String from,
                                     String to, Vehicle vehicle) {

            // Dijkstra weight = CO2 emission × traffic factor
            List<Road> path = Dijkstra.findBestPath(
                    graph, from, to, vehicle,
                    (road, v) -> {
                        double co2PerKm;
                        switch (v.getFuelType().toLowerCase()) {
                            case "electric": return 0.001;
                            case "diesel":   co2PerKm = 0.27; break;
                            default:         co2PerKm = 0.21; break;
                        }
                        double trafficFactor;
                        switch (road.getTrafficLevel().toUpperCase()) {
                            case "HIGH":   trafficFactor = 1.4; break;
                            case "MEDIUM": trafficFactor = 1.1; break;
                            default:       trafficFactor = 1.0; break;
                        }
                        return road.getDistanceKm() * co2PerKm * trafficFactor;
                    });

            return buildResult(path, vehicle);
        }

        private RouteResult buildResult(List<Road> path, Vehicle vehicle) {
            if (path == null || path.isEmpty()) return null;

            List<String> cities = new ArrayList<>();
            cities.add(path.get(0).getFromCity());

            double totalDist     = 0;
            double totalFuel     = 0;
            double totalEmission = 0;
            double totalTime     = 0;

            for (Road road : path) {
                cities.add(road.getToCity());
                totalDist     += road.getDistanceKm();
                totalFuel     += plainFuel(road, vehicle);
                totalEmission += plainEmission(road, vehicle);
                totalTime     += plainTime(road);
            }

            return new RouteResult(cities, totalDist, totalFuel,
                    totalEmission, totalTime,
                    getStrategyName());
        }

        @Override
        public String getStrategyName() { return "ECO_FRIENDLY"; }
    }
