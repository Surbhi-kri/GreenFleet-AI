package org.example;

import org.example.cache.LRUCache;
import org.example.db.DatabaseUtil;
import org.example.db.datasource.PooledDataSourceFactory;
import org.example.model.Graph;
import org.example.model.RouteResult;
import org.example.model.Vehicle;
import org.example.service.RouteService;
import org.example.strategy.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Scanner;

public class App {
    // Pooling configured for route subsystem only
    private static final DataSource routeDs =
            PooledDataSourceFactory.createHikari(
                    "jdbc:postgresql://localhost:5432/greenfleet",
                    "greenfleet_user",
                    "greenfleet_pass",
                    5
            );

    private static final RouteService routeService = new RouteService(routeDs);
    private static final LRUCache cache = new LRUCache(100);
    private static final RoutingContext routingContext =
            new RoutingContext(new EcoFriendlyStrategy());
    private static final Scanner scanner = new Scanner(System.in);
    private static Graph graph = null;

    public static void main(String[] args) {

        try {
            DatabaseUtil.getConnection();
            graph = routeService.loadGraph();
        } catch (SQLException e) {
            System.out.println("Error connecting to database.");
            System.out.println("Make sure Docker is running: docker-compose up -d");
            return;
        }

        System.out.println("========================================");
        System.out.println("    GreenFleet AI — Route Optimizer     ");
        System.out.println("========================================");

        // ask source, destination, vehicle once
        System.out.println("\n  Available cities:");
        System.out.println("  Mumbai, Pune, Nagpur, Delhi, Hyderabad, Bangalore");

        System.out.print("\n  Enter source city      : ");
        String from = scanner.nextLine().trim();

        System.out.print("  Enter destination city : ");
        String to = scanner.nextLine().trim();

        System.out.print("  Enter vehicle ID       : ");
        long vehicleId = readLong();

        // validate vehicle
        Vehicle vehicle = null;
        try {
            vehicle = routeService.getVehicle(vehicleId);
        } catch (SQLException e) {
            System.out.println("  Database error: " + e.getMessage());
            return;
        }

        if (vehicle == null) {
            System.out.println("\n  Vehicle not found.");
            showVehicles();
            return;
        }

        if (!graph.hasCity(from)) {
            System.out.println("\n  City not found: " + from);
            return;
        }

        System.out.println("\n  Vehicle : " + vehicle);
        System.out.println("  Route   : " + from + " → " + to);

        // strategy loop
        boolean running = true;
        while (running) {

            System.out.println("\n========================================");
            System.out.println("  Select a Routing Strategy:");
            System.out.println("  1. Fastest      (minimize travel time)");
            System.out.println("  2. Eco Friendly (minimize CO2 emission)");
            System.out.println("  0. Exit");
            System.out.println("========================================");
            System.out.print("  Enter choice: ");

            int choice = readInt();

            if (choice == 0) {
                running = false;
                continue;
            }

            switch (choice) {
                case 1 -> routingContext.setStrategy(new FastestRouteStrategy());
                case 2 -> routingContext.setStrategy(new EcoFriendlyStrategy());
                default -> {
                    System.out.println("  Invalid choice. Try again.");
                    continue;
                }
            }

            // run Dijkstra
            String cacheKey = from.toLowerCase() + "|" +
                    to.toLowerCase() + "|" +
                    routingContext.getStrategyName() + "|" +
                    vehicleId;

            RouteResult result = cache.get(cacheKey);

            try {
                if (result != null) {
                    printResult(result, true);
                } else {
                    result = routingContext.execute(graph, from, to, vehicle);

                    if (result == null || result.getPath().isEmpty()) {
                        System.out.println("\n  No route found between "
                                + from + " and " + to);
                        continue;
                    }

                    cache.put(cacheKey, result);
                    routeService.saveRoute(result);
                    printResult(result, false);
                }
            } catch (SQLException e) {
                System.out.println("  Database error: " + e.getMessage());
                continue;
            }

            // ask try another strategy?
            System.out.println("\n----------------------------------------");
            System.out.println("  Want to try another strategy?");
            System.out.println("  1. Yes — compare with other strategy");
            System.out.println("  2. No  — exit");
            System.out.println("----------------------------------------");
            System.out.print("  Enter choice: ");

            if (readInt() != 1) {
                running = false;
            }
        }

        // show cache stats on exit
        System.out.println();
        cache.printStats();
        DatabaseUtil.closeConnection();
        System.out.println("\n  Goodbye!");
        System.out.println("========================================");
    }

    // ── Print Result ──────────────────────────────────────────────────────
    private static void printResult(RouteResult result, boolean fromCache) {
        System.out.println("\n========================================");
        System.out.println("         BEST ROUTE RESULT              ");
        System.out.println("========================================");
        if (fromCache) {
            System.out.println("  (served from cache)");
        }
        System.out.println("  Strategy   : " + result.getAlgorithmUsed());
        System.out.println("  Path       : " + result.getPathString());
        System.out.printf("  Distance   : %.1f km%n",
                result.getTotalDistanceKm());
        System.out.printf("  Fuel used  : %.2f L%n",
                result.getTotalFuelL());
        System.out.printf("  CO2 emitted: %.2f kg%n",
                result.getTotalEmissionKg());
        double totalMinutes = result.getTotalTime();
        long hours = (long) (totalMinutes / 60);
        long minutes = Math.round(totalMinutes % 60);
        System.out.printf("  ETA        : %d hr %d min%n", hours, minutes);
        System.out.println("========================================");
    }

    // ── Show vehicles ─────────────────────────────────────────────────────
    private static void showVehicles() {
        try {
            System.out.println("\n  Available Vehicles:");
            routeService.printAllVehicles();
        } catch (SQLException e) {
            System.out.println("  Database error: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private static int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static long readLong() {
        try {
            return Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
