package org.example.db;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JDBCExecutorTest extends TestCase {
    private static Connection connection;
    private static JDBCExecutor jdbc;
    private static boolean initialized = false;

    @Override
    protected void setUp() throws Exception {
        if (!initialized) {
            connection = DriverManager.getConnection("jdbc:h2:mem:jdbc_executor_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
            jdbc = new JDBCExecutor(connection);
            runSqlScript(connection, "db/test-init.sql");
            initialized = true;
        }
    }

    public void testExecuteWithVarArgsInsertsRow() {
        jdbc.execute(
                "INSERT INTO company(company_id, company_name, industry) VALUES (?,?,?)",
                100L, "Fleet 100", "Transport"
        );

        Long companyId = jdbc.findOne(
                "SELECT company_id FROM company WHERE company_id = ?",
                rs -> {
                    try {
                        return rs.getLong("company_id");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                100L
        );

        assertEquals(Long.valueOf(100L), companyId);
    }

    public void testExecuteWithConsumerInsertsRow() {
        jdbc.execute("INSERT INTO company(company_id, company_name, industry) VALUES (?,?,?)", ps -> {
            try {
                ps.setLong(1, 101L);
                ps.setString(2, "Fleet 101");
                ps.setString(3, "Logistics");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        String name = jdbc.findOne(
                "SELECT company_name FROM company WHERE company_id = ?",
                rs -> {
                    try {
                        return rs.getString("company_name");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                101L
        );

        assertEquals("Fleet 101", name);
    }

    public void testFindOneReturnsNullWhenNoRows() {
        String companyName = jdbc.findOne(
                "SELECT company_name FROM company WHERE company_id = ?",
                rs -> {
                    try {
                        return rs.getString("company_name");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                9999L
        );

        assertNull(companyName);
    }

    public void testFindOneThrowsWhenMoreThanOneRow() {
        try {
            jdbc.findOne(
                    "SELECT company_name FROM company",
                    rs -> {
                        try {
                            return rs.getString("company_name");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Expected 0 or 1 row"));
        }
    }

    public void testFindManyReturnsRows() {
        List<String> names = jdbc.findMany(
                "SELECT company_name FROM company ORDER BY company_id",
                rs -> {
                    try {
                        return rs.getString("company_name");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        assertFalse(names.isEmpty());
    }

    public void testFindManyReturnsEmptyList() {
        List<String> names = jdbc.findMany(
                "SELECT company_name FROM company WHERE company_id < 0",
                rs -> {
                    try {
                        return rs.getString("company_name");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        assertTrue(names.isEmpty());
    }

    private static void runSqlScript(Connection con, String resourcePath) throws Exception {
        String sql = loadResource(resourcePath);
        for (String statement : splitStatements(sql)) {
            try (PreparedStatement ps = con.prepareStatement(statement)) {
                ps.execute();
            }
        }
    }

    private static String loadResource(String resourcePath) throws Exception {
        InputStream in = JDBCExecutorTest.class.getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalStateException("Missing resource: " + resourcePath);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.startsWith("--")) {
                    sb.append(line).append('\n');
                }
            }
        }
        return sb.toString();
    }

    private static List<String> splitStatements(String sql) {
        String[] parts = sql.split(";");
        List<String> statements = new ArrayList<>();
        for (String part : parts) {
            String stmt = part.trim();
            if (!stmt.isEmpty()) {
                statements.add(stmt);
            }
        }
        return statements;
    }
}
