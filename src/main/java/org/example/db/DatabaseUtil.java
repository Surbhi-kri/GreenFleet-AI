package org.example.db;
import java.sql.*;
public class DatabaseUtil {
    private static String URL="jdbc:postgresql://localhost:5432/greenfleet";
    private static String USER="greenfleet_user";
    private static String PASSWORD="greenfleet_pass";

    public static Connection con=null;
   public static Connection getConnection() throws SQLException
    {
        if(con==null || con.isClosed())
        {
                con=DriverManager.getConnection(URL,USER,PASSWORD);
            System.out.println("Database Connected to PostgreSQL");
        }
        return con;

    }
    public static void closeConnection()
    {
        try{
            if(con!=null && !con.isClosed())
            {
                con.close();
                System.out.println("Database connection closed");
            }
        }catch(SQLException e)
        {
            System.err.println("Database ERROR:"+ e.getMessage());
        }
    }
}