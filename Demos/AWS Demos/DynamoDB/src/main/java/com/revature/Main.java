package com.revature;

import com.revature.models.Department;
import com.revature.models.User;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.security.SecureRandom;
import java.util.UUID;

public class Main {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static void main(String[] args) {
        DynamoDbClient ddb = DynamoDbClient.builder().build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();

        DynamoDbTable<User> usersTable =
                enhancedClient.table("users", TableSchema.fromBean(User.class));
        DynamoDbTable<Department> deptTable =
                enhancedClient.table("departments", TableSchema.fromBean(Department.class));

        String deptId = "12345";
        String newUserId = UUID.randomUUID().toString();
        String newUsername = randomUsername();

        // Create
        Department department = new Department(deptId, "IT");
        deptTable.putItem(department);

        User user = new User(newUserId, newUsername, "Pass123", "Kyle", "Plummer", deptId, "Admin");
        usersTable.putItem(user);

        // Read one user — key is now (dept_id, username)
        User fetched = usersTable.getItem(Key.builder()
                .partitionValue(deptId)
                .sortValue(newUsername)
                .build());
        System.out.println("---- New User ----\n" + fetched);

        // Query — all users in a department, the access pattern this key design was built for
        QueryConditional queryConditional =
                QueryConditional.keyEqualTo(Key.builder().partitionValue(deptId).build());

        System.out.println("===================================================");
        System.out.println("Users in department " + deptId + ":");
        usersTable.query(queryConditional)
                .items()
                .forEach(u -> System.out.println( u.getRole() + ": " + u));

        // Update
        fetched.setRole("Manager");
        usersTable.updateItem(fetched);

        // Scan — all Admins, across every department
        Expression filterExpression = Expression.builder()
                .expression("#role = :role")
                .putExpressionName("#role", "role")
                .putExpressionValue(":role", AttributeValue.builder().s("Admin").build())
                .build();

        System.out.println("===================================================");
        System.out.println("All Admins (scan):");
        usersTable.scan(ScanEnhancedRequest.builder().filterExpression(filterExpression).build())
                .items()
                .forEach(System.out::println);

        // Delete
        usersTable.deleteItem(Key.builder()
                .partitionValue(deptId)
                .sortValue("kplummer")
                .build());

        System.out.println("===================================================");
    }

    public static String randomUsername() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}