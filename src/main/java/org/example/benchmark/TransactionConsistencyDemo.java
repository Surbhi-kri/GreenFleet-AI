package org.example.benchmark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionConsistencyDemo {
    private static final String URL = "jdbc:postgresql://localhost:5432/greenfleet";
    private static final String USER = "greenfleet_user";
    private static final String PASSWORD = "greenfleet_pass";

    public static void main(String[] args) throws Exception {
        System.out.println("=== Transaction Consistency Demo ===");
        long routeId = ensureDemoRoute();

        long tripWithoutTx = runWithoutTransaction(routeId);
        printInconsistencyState("WITHOUT TRANSACTION", tripWithoutTx);

        Long tripWithTx = runWithTransaction(routeId);
        printInconsistencyState("WITH TRANSACTION", tripWithTx);
    }

    private static long ensureDemoRoute() throws SQLException {
        String sql = """
            INSERT INTO route(from_city,to_city,path,total_distance,total_fuel_l,total_emission,algorithm_used)
            VALUES ('DemoFrom','DemoTo','DemoFrom -> DemoTo',10,1.0,0.2,'TX_DEMO')
            RETURNING route_id
            """;
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static long runWithoutTransaction(long routeId) throws SQLException {
        long driverId = fetchAnyId("SELECT driver_id FROM driver LIMIT 1");
        long vehicleId = fetchAnyId("SELECT vehicle_id FROM vehicle LIMIT 1");

        String insertTrip = """
            INSERT INTO trip(driver_id, vehicle_id, route_id, actual_fuel_used_l)
            VALUES (?,?,?,?) RETURNING trip_id
            """;

        long tripId;
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(insertTrip)) {
            ps.setLong(1, driverId);
            ps.setLong(2, vehicleId);
            ps.setLong(3, routeId);
            ps.setDouble(4, 1.5);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                tripId = rs.getLong(1);
            }
        }

        try {
            throw new RuntimeException("Simulated failure before emission_log insert");
        } catch (RuntimeException ex) {
            System.out.println("WITHOUT TX simulated error: " + ex.getMessage());
        }

        return tripId;
    }

    private static Long runWithTransaction(long routeId) throws SQLException {
        long driverId = fetchAnyId("SELECT driver_id FROM driver LIMIT 1");
        long vehicleId = fetchAnyId("SELECT vehicle_id FROM vehicle LIMIT 1");

        String insertTrip = """
            INSERT INTO trip(driver_id, vehicle_id, route_id, actual_fuel_used_l)
            VALUES (?,?,?,?) RETURNING trip_id
            """;

        Long tripId = null;
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
            con.setAutoCommit(false);
            try (PreparedStatement tripPs = con.prepareStatement(insertTrip)) {
                tripPs.setLong(1, driverId);
                tripPs.setLong(2, vehicleId);
                tripPs.setLong(3, routeId);
                tripPs.setDouble(4, 1.7);

                try (ResultSet rs = tripPs.executeQuery()) {
                    rs.next();
                    tripId = rs.getLong(1);
                }

                throw new RuntimeException("Simulated failure in transaction");
            } catch (Exception e) {
                con.rollback();
                System.out.println("WITH TX rolled back: " + e.getMessage());
                return tripId;
            }
        }
    }

    private static long fetchAnyId(String query) throws SQLException {
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new IllegalStateException("Required demo reference data is missing for query: " + query);
            }
            return rs.getLong(1);
        }
    }

    private static void printInconsistencyState(String label, Long tripId) throws SQLException {
        if (tripId == null) {
            System.out.println(label + " -> trip not persisted");
            return;
        }

        String sql = """
            SELECT
              EXISTS(SELECT 1 FROM trip WHERE trip_id = ?) AS trip_exists,
              EXISTS(SELECT 1 FROM emission_log WHERE trip_id = ?) AS emission_exists
            """;

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, tripId);
            ps.setLong(2, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                boolean tripExists = rs.getBoolean("trip_exists");
                boolean emissionExists = rs.getBoolean("emission_exists");
                System.out.printf("%s -> trip_exists=%s, emission_exists=%s%n", label, tripExists, emissionExists);
            }
        }
    }
}
