package org.example.db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


public class JDBCExecutor{
    private final Connection con;
    public JDBCExecutor(Connection con){
      this.con=con;
    }

    public void execute(String query,Object... args)
    {
        try(PreparedStatement ps=con.prepareStatement(query)){
            bindArgs(ps,args);
            ps.executeUpdate();

        }catch(SQLException e)
        {
            throw new RuntimeException("Failed to execute update:" + query,e);
        }
    }
    public void execute(String query, Consumer<PreparedStatement> binder) {
        try (PreparedStatement ps = con.prepareStatement(query)) {
            binder.accept(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute update with binder: " + query, e);
        }
    }
    public <T> T findOne(String query, Function<ResultSet, T> mapper, Object... args) {
        try (PreparedStatement ps = con.prepareStatement(query)) {
            bindArgs(ps, args);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null; // 0 rows
                }

                T value = mapper.apply(rs); // 1st row

                if (rs.next()) {
                    throw new IllegalStateException("Expected 0 or 1 row, but got more than 1 for query: " + query);
                }

                return value;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute findOne: " + query, e);
        }
    }
    public <T> List<T> findMany(String query, Function<ResultSet, T> mapper, Object... args) {
        try (PreparedStatement ps = con.prepareStatement(query)) {
            bindArgs(ps, args);

            try (ResultSet rs = ps.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapper.apply(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute findMany: " + query, e);
        }
    }

    private void bindArgs(PreparedStatement ps, Object... args) throws SQLException {
        if (args == null) return;
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }
}