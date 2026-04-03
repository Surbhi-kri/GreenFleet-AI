package org.example.dao;

import junit.framework.TestCase;
import org.example.db.JDBCExecutor;
import org.example.model.RouteResult;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RouteDaoTest extends TestCase {
    private static Connection connection;
    private static RouteDao routeDao;
    private static boolean initialized = false;

    @Override
    protected void setUp() throws Exception {
        if (!initialized) {
            connection = DriverManager.getConnection("jdbc:h2:mem:route_dao_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
            JDBCExecutor jdbcExecutor = new JDBCExecutor(connection);
            routeDao = new RouteDao(jdbcExecutor);
            runSqlScript(connection, "db/test-init.sql");
            initialized = true;
        }
    }

    public void testInsertAndFindAll() {
        RouteResult routeResult = new RouteResult(
                Arrays.asList("Mumbai", "Pune", "Delhi"),
                1400.0,
                112.0,
                310.0,
                0.0,
                "ECO_FRIENDLY"
        );

        long before = routeDao.countAll();
        routeDao.insert(routeResult);
        long after = routeDao.countAll();

        assertEquals(before + 1, after);
    }

    public void testFindAllReturnsMappedRows() {
        List<RouteResult> routes = routeDao.findAll();
        assertFalse(routes.isEmpty());
        assertNotNull(routes.get(0).getPath());
        assertNotNull(routes.get(0).getAlgorithmUsed());
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
        InputStream in = RouteDaoTest.class.getClassLoader().getResourceAsStream(resourcePath);
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
