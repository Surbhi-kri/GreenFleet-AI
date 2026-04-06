package org.example.strategy;

import org.example.model.Graph;
import org.example.model.Road;
import org.example.model.Vehicle;

import java.util.*;

/**
 * Dijkstra's shortest path algorithm.
 *
 * HOW IT WORKS:
 * 1. Start at source city with cost = 0
 * 2. Use a priority queue — always process the city with LOWEST cost next
 * 3. For each neighbour, calculate: cost to reach it = cost so far + edge weight
 * 4. If this new cost is better than what we knew before — update it
 * 5. Repeat until we reach destination
 *
 * The WeightFunction decides what "cost" means:
 *   Fastest      → cost = distance × traffic multiplier
 *   FuelEfficient → cost = fuel used on this road
 *   EcoFriendly  → cost = CO2 emitted on this road
 *
 * Same algorithm, different weights = Strategy Pattern!
 */
public class Dijkstra {

    /**
     * Functional interface — each strategy provides its own weight function.
     * This is what makes Dijkstra work differently for each strategy.
     */
    @FunctionalInterface
    public interface WeightFunction {
        double weight(Road road, Vehicle vehicle);
    }

    /**
     * Runs Dijkstra and returns the best path from source to destination.
     *
     * @param graph          the city road network
     * @param source         starting city
     * @param destination    target city
     * @param vehicle        vehicle making the trip
     * @param weightFn       how to calculate edge cost (varies per strategy)
     * @return list of city names forming the best path, or empty list if no path
     */

    public static List<Road> findBestPath(Graph graph, String source,
                                          String destination, Vehicle vehicle,
                                          WeightFunction weightFn) {

        // cost[city] = lowest cost found so far to reach that city
        Map<String, Double> cost = new HashMap<>();

        // previous[city] = the road we took to reach this city on best path
        Map<String, Road> previous = new HashMap<>();

        // visited = cities we already finalised
        Set<String> visited = new HashSet<>();

        // Priority queue: [cost, cityName] — always picks lowest cost city next
        PriorityQueue<double[]> pq = new PriorityQueue<>(
                Comparator.comparingDouble(a -> a[0]));

        // City index for priority queue (we store index not name)
        Map<String, Integer> cityIndex = new HashMap<>();
        List<String> cities = new ArrayList<>();

        // Helper to get/create city index
        java.util.function.Function<String, Integer> getIdx = city -> {
            if (!cityIndex.containsKey(city)) {
                cityIndex.put(city, cities.size());
                cities.add(city);
            }
            return cityIndex.get(city);
        };

        // Initialise source
        cost.put(source, 0.0);
        pq.offer(new double[]{0.0, getIdx.apply(source)});

        while (!pq.isEmpty()) {
            double[] curr    = pq.poll();
            double   currCost = curr[0];
            String   currCity = cities.get((int) curr[1]);

            if (visited.contains(currCity)) continue;
            visited.add(currCity);

            // Found destination — stop early
            if (currCity.equals(destination)) break;

            // Explore all roads leaving currCity
            for (Road road : graph.getNeighbours(currCity)) {
                String neighbour = road.getToCity();
                if (visited.contains(neighbour)) continue;

                // Calculate cost of taking this road
                double edgeCost  = weightFn.weight(road, vehicle);
                double totalCost = currCost + edgeCost;

                // If this path is cheaper — update
                if (totalCost < cost.getOrDefault(neighbour, Double.MAX_VALUE)) {
                    cost.put(neighbour, totalCost);
                    previous.put(neighbour, road);
                    pq.offer(new double[]{totalCost, getIdx.apply(neighbour)});
                }
            }
        }

        // Reconstruct path by following previous[] map backwards
        return reconstructPath(previous, source, destination);
    }

    /**
     * Walks back through previous[] map to build the path from source to dest.
     */
    private static List<Road> reconstructPath(Map<String, Road> previous,
                                              String source, String destination) {
        LinkedList<Road> path = new LinkedList<>();
        String current = destination;

        while (previous.containsKey(current)) {
            Road road = previous.get(current);
            path.addFirst(road);
            current = road.getFromCity();
        }

        // If we never reached destination, return empty
        if (path.isEmpty() || !path.getFirst().getFromCity().equals(source)) {
            return new ArrayList<>();
        }

        return path;
    }
}