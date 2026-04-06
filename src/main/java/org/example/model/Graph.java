package org.example.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Graph holds all roads as an adjacency list.
 * Cities = nodes, Roads = edges with weights.
 *
 * adjacencyList:
 *   "Mumbai" → [Road(Mumbai→Pune,148), Road(Mumbai→Nagpur,820), ...]
 *   "Pune"   → [Road(Pune→Hyderabad,560), ...]
 */
public class Graph{
    private final Map<String,List<Road>> adjacencyList=new HashMap<>();

    public void addRoad(Road road){
        adjacencyList.computeIfAbsent(road.getFromCity(),k->new ArrayList<>()).add(road);


    }
    public List<Road> getNeighbours(String city) {
        return adjacencyList.getOrDefault(city, new ArrayList<>());
    }

    public boolean hasCity(String city) {
        return adjacencyList.containsKey(city);
    }

    public void printGraph() {
        System.out.println("  City Network:");
        for (Map.Entry<String, List<Road>> entry : adjacencyList.entrySet()) {
            for (Road r : entry.getValue()) {
                System.out.printf("    %s → %s  %.0fkm  %s  %s%n",
                        r.getFromCity(), r.getToCity(),
                        r.getDistanceKm(), r.getRoadType(), r.getTrafficLevel());
            }
        }
    }
}