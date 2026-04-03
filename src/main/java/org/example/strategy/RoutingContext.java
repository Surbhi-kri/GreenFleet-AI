package org.example.strategy;

import org.example.model.Graph;
import org.example.model.RouteResult;
import org.example.model.Vehicle;

public class RoutingContext {

    private RouteStrategy strategy;

    // default strategy is now EcoFriendly
    public RoutingContext() {
        this.strategy = new EcoFriendlyStrategy();
    }

    public RoutingContext(RouteStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(RouteStrategy strategy) {
        this.strategy = strategy;
    }

    public String getStrategyName() {
        return strategy.getStrategyName();
    }

    public RouteResult execute(Graph graph, String from,
                               String to, Vehicle vehicle) {
        return strategy.calculate(graph, from, to, vehicle);
    }
}