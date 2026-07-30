package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    public static Connection getConnection(String url) {
        try( Connection conn = DriverManager.getConnection(url)) {
            return conn;
        } catch (SQLException e) {
            //This isn't really a good way to handle exceptions, but for now...
            e.printStackTrace();
        }
        return null;
    }

    public static Connection getConnection() {
        return getConnection("jdbc:sqlite:C:/sqlite/databases/fnma26");
    }
    
}
