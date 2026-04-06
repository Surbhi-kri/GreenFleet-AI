package org.example.benchmark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IsolationLevelDemo {
    private static final String URL = "jdbc:postgresql://localhost:5432/greenfleet";
    private static final String USER = "greenfleet_user";
    private static final String PASSWORD = "greenfleet_pass";

    public static void main(String[] args) throws Exception {
        System.out.println("=== Isolation Demo: Non-repeatable Read on route.total_emission ===");
        long routeId = ensureIsolationRoute();

        runScenario("READ COMMITTED", Connection.TRANSACTION_READ_COMMITTED, routeId);
        runScenario("REPEATABLE READ", Connection.TRANSACTION_REPEATABLE_READ, routeId);
    }

    private static long ensureIsolationRoute() throws SQLException {
        String sql = """
            INSERT INTO route(from_city,to_city,path,total_distance,total_fuel_l,total_emission,algorithm_used)
            VALUES ('IsoA','IsoB','IsoA -> IsoB',100,8,20,'ISO_DEMO')
            RETURNING route_id
            """;
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static void runScenario(String label, int isolation, long routeId) throws Exception {
        try (Connection reader = DriverManager.getConnection(URL, USER, PASSWORD);
             Connection writer = DriverManager.getConnection(URL, USER, PASSWORD)) {

            reader.setAutoCommit(false);
            writer.setAutoCommit(false);

            reader.setTransactionIsolation(isolation);
            writer.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            double first = readEmission(reader, routeId);

            updateEmission(writer, routeId, first + 5.0);
            writer.commit();

            double second = readEmission(reader, routeId);
            reader.commit();

            System.out.printf("%s -> first_read=%.3f second_read=%.3f%n", label, first, second);
        }
    }

    private static double readEmission(Connection con, long routeId) throws SQLException {
        String sql = "SELECT total_emission FROM route WHERE route_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, routeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Route missing for id=" + routeId);
                }
                return rs.getDouble(1);
            }
        }
    }

    private static void updateEmission(Connection con, long routeId, double newEmission) throws SQLException {
        String sql = "UPDATE route SET total_emission = ? WHERE route_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, newEmission);
            ps.setLong(2, routeId);
            ps.executeUpdate();
        }
    }
}
