package com.revature.demo.redshift;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    // Fill in with your sandbox cluster/workgroup endpoint. IAM=true + DbUser means
    // no password is ever specified -- the driver resolves credentials the same way
    // DynamoDbClient.builder().build() did, via your local IAM role/credentials.
    private static final String JDBC_URL =
            "jdbc:redshift:iam://fnma26-workgroup.727691927255.us-east-1.redshift-serverless.amazonaws.com:5439/dev";

    public static void main(String[] args) throws SQLException {
        try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
            insertSampleData(conn);
            runAggregateQuery(conn);
            runSortKeyDemo(conn);

//            Statement stmt = conn.createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT current_user;");
//            rs.next();
//            System.out.println(rs.getString(1));
        }
    }

    // ---- Live CRUD equivalent: seed a handful of rows, not a real bulk load ----
    private static void insertSampleData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO stores VALUES (1, 'Austin', 'South'), (2, 'Chicago', 'Midwest')");

            stmt.execute("INSERT INTO products VALUES "
                    + "(1, 'Drip Coffee', 'Beverage', 3.25), "
                    + "(2, 'Croissant', 'Bakery', 2.75)");

            stmt.execute("INSERT INTO sales VALUES "
                    + "(1, 1, 1, '2026-06-01', 2, 6.50), "
                    + "(2, 1, 2, '2026-06-01', 1, 2.75), "
                    + "(3, 2, 1, '2026-06-15', 5, 16.25), "
                    + "(4, 2, 2, '2026-07-02', 3, 8.25), "
                    + "(5, 1, 1, '2026-07-10', 4, 13.00)");
        }
        System.out.println("Seeded stores, products, and sales.");
    }

    // ---- The core analytics access pattern: aggregate across everything ----
    private static void runAggregateQuery(Connection conn) throws SQLException {
        String sql = "SELECT st.region, SUM(sa.total) AS revenue " +
                "FROM sales sa JOIN stores st ON sa.store_id = st.store_id " +
                "GROUP BY st.region " +
                "ORDER BY revenue DESC";

        System.out.println("===================================================");
        System.out.println("Revenue by region:");
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.println(rs.getString("region") + ": $" + rs.getBigDecimal("revenue"));
            }
        }
    }

    // ---- Sort key pruning demo (Query vs Scan equivalent) ----
    // With this little data, both plans will look similar -- there's only one block
    // to begin with. Talk through what EXPLAIN would show at real scale: a predicate
    // on sale_date lets Redshift skip whole blocks via zone maps, the same way Query
    // only read one partition instead of the whole Orders table.
    private static void runSortKeyDemo(Connection conn) throws SQLException {
        String withPredicate = "EXPLAIN SELECT * FROM sales WHERE sale_date >= '2026-07-01'";
        String withoutPredicate = "EXPLAIN SELECT * FROM sales";

        System.out.println("===================================================");
        System.out.println("Plan WITH sort-key predicate:");
        printExplain(conn, withPredicate);

        System.out.println("\nPlan WITHOUT sort-key predicate (full scan):");
        printExplain(conn, withoutPredicate);
    }

    private static void printExplain(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        }
    }
}