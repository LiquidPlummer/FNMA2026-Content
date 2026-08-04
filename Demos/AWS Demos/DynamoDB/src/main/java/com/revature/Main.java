package com.revature;

import com.revature.models.Department;
import com.revature.models.User;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        DynamoDbClient ddb = DynamoDbClient.builder().build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();

        DynamoDbTable<User> usersTable =
                enhancedClient.table("users", TableSchema.fromBean(User.class));
        DynamoDbTable<Department> deptTable =
                enhancedClient.table("departments", TableSchema.fromBean(Department.class));

        String deptId = "ab1be53a-c994-472f-9c5e-f83050db846b";
        String newUserId = UUID.randomUUID().toString();

        Department department = new Department(deptId, "IT");
        deptTable.putItem(department);

        User user = new User(newUserId, "kplummer", "Pass123", "Kyle", "Plummer", deptId, "Admin");
        usersTable.putItem(user);



//        Order fetched = orderTable.getItem(Key.builder()
//                .partitionValue("cust-1001")
//                .sortValue("order-5001")
//                .build());
//
//        QueryConditional queryConditional =
//                QueryConditional.keyEqualTo(Key.builder().partitionValue("cust-1001").build());
//
//        orderTable.query(queryConditional)
//                .items()
//                .forEach(o -> System.out.println(o.getOrderId() + ": " + o.getStatus()));
//
//        fetched.setStatus("SHIPPED");
//        orderTable.updateItem(fetched);

//        orderTable.deleteItem(Key.builder()
//                .partitionValue("cust-1001")
//                .sortValue("order-5001")
//                .build());

        QueryConditional queryConditional =
                QueryConditional.keyEqualTo(Key.builder().partitionValue("1").build());


        System.out.println("===================================================");
        System.out.println("Queried user: ");
        usersTable.query(queryConditional)
                .items()
                .forEach(o -> System.out.println(o));
        System.out.println("\n");
        usersTable.scan()
                .items()
                .forEach(o -> System.out.println(o));

        System.out.println("===================================================");
    }


}
