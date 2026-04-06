package org.example.strategy;

import org.example.model.*;
import java.util.ArrayList;
import java.util.List;

public class FastestRouteStrategy implements RouteStrategy {

    @Override
    public RouteResult calculate(Graph graph, String from,
                                 String to, Vehicle vehicle) {

        // Dijkstra weight = time in minutes using ALL factors
        // traffic + road type + speed limit
        List<Road> path = Dijkstra.findBestPath(
                graph, from, to, vehicle,
                (road, v) -> plainTime(road));  // ← uses full time formula

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
    public String getStrategyName() { return "FASTEST"; }
}