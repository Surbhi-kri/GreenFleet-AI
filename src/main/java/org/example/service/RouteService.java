package org.example.service;

import org.example.db.DatabaseUtil;
import org.example.db.JDBCExecutor;
import org.example.model.Graph;
import org.example.model.Road;
import org.example.model.RouteResult;
import org.example.model.Vehicle;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class RouteService {
    private final JDBCExecutor jdbc;
    private final DataSource routeDataSource;

    public RouteService() {
        try {
            this.jdbc = new JDBCExecutor(DatabaseUtil.getConnection());
            this.routeDataSource = null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize RouteService DB executor", e);
        }
    }

    // Route subsystem constructor using DataSource (single-connection or pooled)
    public RouteService(DataSource routeDataSource) {
        this.routeDataSource = routeDataSource;
        this.jdbc = null;
    }

    // Load entire road network from DB into a Graph object
    public Graph loadGraph() throws SQLException {
        Graph graph = new Graph();
        String sql = "SELECT * FROM road";
        try (Statement stmt = DatabaseUtil.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Road forwardRoad = new Road(
                        rs.getLong("road_id"),
                        rs.getString("from_city"),
                        rs.getString("to_city"),
                        rs.getDouble("distance_km"),
                        rs.getString("road_type"),
                        rs.getString("traffic_level"),
                        rs.getInt("speed_limit")
                );

                Road reverseRoad = new Road(
                        rs.getLong("road_id"),
                        rs.getString("to_city"),
                        rs.getString("from_city"),
                        rs.getDouble("distance_km"),
                        rs.getString("road_type"),
                        rs.getString("traffic_level"),
                        rs.getInt("speed_limit")
                );

                graph.addRoad(forwardRoad);
                graph.addRoad(reverseRoad);
            }
        }
        return graph;
    }

    public Vehicle getVehicle(long vehicleId) throws SQLException {
        String sql = "SELECT * FROM vehicle WHERE vehicle_id = ?";
        if (routeDataSource == null) {
            return mapVehicle(jdbc, sql, vehicleId);
        }
        try (Connection con = routeDataSource.getConnection()) {
            JDBCExecutor executor = new JDBCExecutor(con);
            return mapVehicle(executor, sql, vehicleId);
        }
    }

    public void saveRoute(RouteResult result) throws SQLException {
        String sql = """
            INSERT INTO route
              (from_city, to_city, path, total_distance,
               total_fuel_l, total_emission, algorithm_used)
            VALUES (?,?,?,?,?,?,?)
            """;

        String from = result.getPath().get(0);
        String to = result.getPath().get(result.getPath().size() - 1);

        if (routeDataSource == null) {
            jdbc.execute(sql,
                    from,
                    to,
                    result.getPathString(),
                    result.getTotalDistanceKm(),
                    result.getTotalFuelL(),
                    result.getTotalEmissionKg(),
                    result.getAlgorithmUsed());
            return;
        }

        try (Connection con = routeDataSource.getConnection()) {
            JDBCExecutor executor = new JDBCExecutor(con);
            executor.execute(sql,
                    from,
                    to,
                    result.getPathString(),
                    result.getTotalDistanceKm(),
                    result.getTotalFuelL(),
                    result.getTotalEmissionKg(),
                    result.getAlgorithmUsed());
        }
    }

    public void printAllRoutes() throws SQLException {
        String sql = "SELECT * FROM route ORDER BY calculated_at DESC";
        List<String> lines;

        if (routeDataSource == null) {
            lines = mapRouteLines(jdbc, sql);
        } else {
            try (Connection con = routeDataSource.getConnection()) {
                JDBCExecutor executor = new JDBCExecutor(con);
                lines = mapRouteLines(executor, sql);
            }
        }

        if (lines.isEmpty()) {
            System.out.println("  No routes saved yet.");
            return;
        }

        for (String line : lines) {
            System.out.println(line);
        }
    }

    public void printAllVehicles() throws SQLException {
        String sql = "SELECT * FROM vehicle ORDER BY vehicle_id";
        try (Statement stmt = DatabaseUtil.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("  [%d] %-12s | %-8s | %-10s | %.1f kmpl%n",
                        rs.getLong("vehicle_id"),
                        rs.getString("registration_no"),
                        rs.getString("vehicle_type"),
                        rs.getString("fuel_type"),
                        rs.getDouble("fuel_efficiency_kmpl"));
            }
        }
    }

    private Vehicle mapVehicle(JDBCExecutor executor, String sql, long vehicleId) {
        return executor.findOne(sql, rs -> {
            try {
                return new Vehicle(
                        rs.getLong("vehicle_id"),
                        rs.getLong("company_id"),
                        rs.getString("registration_no"),
                        rs.getString("vehicle_type"),
                        rs.getDouble("fuel_efficiency_kmpl"),
                        rs.getString("fuel_type")
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, vehicleId);
    }

    private List<String> mapRouteLines(JDBCExecutor executor, String sql) {
        return executor.findMany(sql, rs -> {
            try {
                return String.format(
                        "  [%d] %s%n       dist=%.0fkm | fuel=%.2fL | co2=%.2fkg | %s",
                        rs.getLong("route_id"),
                        rs.getString("path"),
                        rs.getDouble("total_distance"),
                        rs.getDouble("total_fuel_l"),
                        rs.getDouble("total_emission"),
                        rs.getString("algorithm_used")
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
