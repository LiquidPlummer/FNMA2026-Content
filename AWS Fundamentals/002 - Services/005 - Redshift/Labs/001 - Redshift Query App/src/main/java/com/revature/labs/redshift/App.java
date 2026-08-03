package com.revature.labs.redshift;

import java.util.List;
import java.util.stream.Collectors;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.redshiftdata.RedshiftDataClient;
import software.amazon.awssdk.services.redshiftdata.model.ColumnMetadata;
import software.amazon.awssdk.services.redshiftdata.model.DescribeStatementRequest;
import software.amazon.awssdk.services.redshiftdata.model.DescribeStatementResponse;
import software.amazon.awssdk.services.redshiftdata.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.redshiftdata.model.ExecuteStatementResponse;
import software.amazon.awssdk.services.redshiftdata.model.Field;
import software.amazon.awssdk.services.redshiftdata.model.GetStatementResultRequest;
import software.amazon.awssdk.services.redshiftdata.model.GetStatementResultResponse;
import software.amazon.awssdk.services.redshiftdata.model.StatusString;

public class App {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS sales (
                id INT,
                region VARCHAR(50),
                category VARCHAR(50),
                amount DECIMAL(10,2)
            )
            """;

    private static final String SEED_DATA_SQL = """
            INSERT INTO sales (id, region, category, amount) VALUES
                (1, 'us-east', 'electronics', 199.99),
                (2, 'us-east', 'books', 24.50),
                (3, 'us-west', 'electronics', 349.00),
                (4, 'us-west', 'books', 15.75),
                (5, 'eu-west', 'electronics', 275.20),
                (6, 'eu-west', 'books', 32.10),
                (7, 'us-east', 'electronics', 89.99),
                (8, 'us-west', 'books', 41.00)
            """;

    private static final String AGGREGATE_QUERY_SQL = """
            SELECT region, SUM(amount) AS total_sales
            FROM sales
            GROUP BY region
            ORDER BY total_sales DESC
            """;

    public static void main(String[] args) {
        String workgroupName = System.getenv().getOrDefault("WORKGROUP_NAME", "redshift-lab-wg");
        String database = System.getenv().getOrDefault("DATABASE_NAME", "dev");
        String regionName = System.getenv().getOrDefault("AWS_REGION", "us-east-1");

        try (RedshiftDataClient client = RedshiftDataClient.builder()
                .region(Region.of(regionName))
                .build()) {

            System.out.println("Creating table...");
            executeAndWait(client, workgroupName, database, CREATE_TABLE_SQL);

            System.out.println("Seeding sample rows...");
            executeAndWait(client, workgroupName, database, SEED_DATA_SQL);

            System.out.println("Running aggregate query: total sales by region...");
            String statementId = executeAndWait(client, workgroupName, database, AGGREGATE_QUERY_SQL);
            printResults(client, statementId);
        }
    }

    /**
     * Submits a SQL statement and blocks until Redshift finishes running it.
     * The Data API is asynchronous: ExecuteStatement returns immediately with
     * just a statement ID, so the caller has to poll DescribeStatement to find
     * out when (and whether) it actually completed.
     */
    private static String executeAndWait(RedshiftDataClient client, String workgroupName, String database,
            String sql) {
        // TODO: submit the statement with client.executeStatement(...), then loop
        // on client.describeStatement(...) — sleeping briefly between checks — until
        // the status is no longer SUBMITTED, PICKED, or STARTED. If it doesn't end in
        // FINISHED, throw a RuntimeException using status.error(). Otherwise return the ID.
        return null;
    }

    /**
     * Fetches and prints the result set of a completed statement.
     */
    private static void printResults(RedshiftDataClient client, String statementId) {
        // TODO: call client.getStatementResult(...) with the statement ID, print the
        // column names from the response's columnMetadata(), then print each row from
        // records() — use the fieldToString helper below to read each Field's value.
    }

    private static String fieldToString(Field field) {
        if (Boolean.TRUE.equals(field.isNull())) {
            return "null";
        }
        return switch (field.type()) {
            case STRING_VALUE -> field.stringValue();
            case LONG_VALUE -> String.valueOf(field.longValue());
            case DOUBLE_VALUE -> String.valueOf(field.doubleValue());
            case BOOLEAN_VALUE -> String.valueOf(field.booleanValue());
            default -> field.toString();
        };
    }
}
