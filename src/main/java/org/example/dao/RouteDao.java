package org.example.dao;

import org.example.db.JDBCExecutor;
import org.example.model.RouteResult;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class RouteDao {
    private final JDBCExecutor jdbc;

    public RouteDao(JDBCExecutor jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(RouteResult routeResult) {
        String sql = """
            INSERT INTO route
              (from_city, to_city, path, total_distance, total_fuel_l, total_emission, algorithm_used)
            VALUES (?,?,?,?,?,?,?)
            """;

        String from = routeResult.getPath().get(0);
        String to = routeResult.getPath().get(routeResult.getPath().size() - 1);

        jdbc.execute(sql,
                from,
                to,
                routeResult.getPathString(),
                routeResult.getTotalDistanceKm(),
                routeResult.getTotalFuelL(),
                routeResult.getTotalEmissionKg(),
                routeResult.getAlgorithmUsed());
    }

    public List<RouteResult> findAll() {
        String sql = """
            SELECT path, total_distance, total_fuel_l, total_emission, algorithm_used
            FROM route
            ORDER BY calculated_at DESC, route_id DESC
            """;

        return jdbc.findMany(sql, rs -> {
            try {
                String pathValue = rs.getString("path");
                List<String> path = Arrays.stream(pathValue.split("\\s*→\\s*")).toList();
                return new RouteResult(
                        path,
                        rs.getDouble("total_distance"),
                        rs.getDouble("total_fuel_l"),
                        rs.getDouble("total_emission"),
                        0.0,
                        rs.getString("algorithm_used")
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public long countAll() {
        Long count = jdbc.findOne("SELECT COUNT(*) AS cnt FROM route", rs -> {
            try {
                return rs.getLong("cnt");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return count == null ? 0L : count;
    }
}
