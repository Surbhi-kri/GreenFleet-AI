package org.example.strategy;

import org.example.model.Graph;
import org.example.model.Road;
import org.example.model.RouteResult;
import org.example.model.Vehicle;

public interface RouteStrategy {

    RouteResult calculate(Graph graph, String from,
                          String to, Vehicle vehicle);

    String getStrategyName();

    // plain fuel — distance / efficiency
    default double plainFuel(Road road, Vehicle vehicle) {
        if (vehicle.getFuelType().equalsIgnoreCase("electric")) return 0.0;
        return road.getDistanceKm() / vehicle.getFuelEfficiencyKmpl();
    }

    // plain emission — distance × co2 per km
    default double plainEmission(Road road, Vehicle vehicle) {
        double co2PerKm;
        switch (vehicle.getFuelType().toLowerCase()) {
            case "electric": return 0.0;
            case "diesel":   co2PerKm = 0.27; break;
            default:         co2PerKm = 0.21; break;
        }
        return road.getDistanceKm() * co2PerKm;
    }

    // plain time — considers distance + traffic + road type + speed limit
    default double plainTime(Road road) {

        // how much traffic slows you down
        double trafficFactor;
        switch (road.getTrafficLevel().toUpperCase()) {
            case "LOW":    trafficFactor = 1.00; break;
            case "MEDIUM": trafficFactor = 0.70; break;
            case "HIGH":   trafficFactor = 0.40; break;
            default:       trafficFactor = 1.00; break;
        }

        // road type affects achievable speed
        double roadTypeFactor;
        switch (road.getRoadType().toUpperCase()) {
            case "EXPRESSWAY": roadTypeFactor = 1.00; break;
            case "HIGHWAY":    roadTypeFactor = 0.85; break;
            case "CITY":       roadTypeFactor = 0.60; break;
            default:           roadTypeFactor = 1.00; break;
        }

        // actual speed = speed_limit × traffic × road type
        double actualSpeed = road.getSpeedLimit()
                * trafficFactor
                * roadTypeFactor;

        if (actualSpeed <= 0) actualSpeed = 10;

        // time in minutes
        return (road.getDistanceKm() / actualSpeed) * 60;
    }
}
