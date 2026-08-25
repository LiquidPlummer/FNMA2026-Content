package com.revature.demo.s3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class S3Demo {
    public static void main(String[] args) throws Exception {
        String host = System.getenv("DB_HOST");
        String port = System.getenv().getOrDefault("DB_PORT", "3306");
        String dbName = System.getenv("DB_NAME");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to " + url);

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS visits (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            visited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                        """);
            }

            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO visits (visited_at) VALUES (NOW())")) {
                insert.executeUpdate();
            }

            System.out.println("All visits so far:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, visited_at FROM visits ORDER BY id")) {
                while (rs.next()) {
                    System.out.printf("  #%d - %s%n", rs.getInt("id"), rs.getTimestamp("visited_at"));
                }
            }
        }
    }
}