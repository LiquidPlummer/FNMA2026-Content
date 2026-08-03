package com.revature.labs.dynamodb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public class App {

    // category = partition key, productId = sort key
    private record Product(String category, String productId, String name, String price) {
    }

    private static final List<Product> SAMPLE_PRODUCTS = List.of(
            new Product("electronics", "E-100", "Wireless Mouse", "19.99"),
            new Product("electronics", "E-101", "USB-C Hub", "34.50"),
            new Product("electronics", "E-102", "Mechanical Keyboard", "89.00"),
            new Product("books", "B-200", "Effective Java", "42.00"),
            new Product("books", "B-201", "Designing Data-Intensive Applications", "55.75"));

    public static void main(String[] args) {
        String tableName = System.getenv().getOrDefault("TABLE_NAME", "products-lab");
        String regionName = System.getenv().getOrDefault("AWS_REGION", "us-east-1");

        try (DynamoDbClient client = DynamoDbClient.builder()
                .region(Region.of(regionName))
                .build()) {

            System.out.println("Seeding " + SAMPLE_PRODUCTS.size() + " products into " + tableName + "...");
            for (Product product : SAMPLE_PRODUCTS) {
                putProduct(client, tableName, product);
            }

            System.out.println("\nLooking up one specific product by its full key (category + productId)...");
            Map<String, AttributeValue> found = getProduct(client, tableName, "electronics", "E-101");
            printItem(found);

            System.out.println("\nQuerying every product in the 'electronics' category...");
            List<Map<String, AttributeValue>> results = queryCategory(client, tableName, "electronics");
            results.forEach(App::printItem);
        }
    }

    private static void putProduct(DynamoDbClient client, String tableName, Product product) {
        // TODO: build the item map DynamoDB expects: a Map<String, AttributeValue>
        // with one entry per attribute. String attributes use AttributeValue.builder().s(...),
        // number attributes use AttributeValue.builder().n(...) (still passed as a String).
        Map<String, AttributeValue> item = null;

        client.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build());
    }

    private static Map<String, AttributeValue> getProduct(DynamoDbClient client, String tableName,
            String category, String productId) {
        // TODO: build the key map for a GetItem request. Unlike putProduct's item map,
        // a key map only needs the partition key ("category") and sort key ("productId") —
        // not every attribute on the item.
        Map<String, AttributeValue> key = null;

        GetItemResponse response = client.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build());
        return response.item();
    }

    private static List<Map<String, AttributeValue>> queryCategory(DynamoDbClient client, String tableName,
            String category) {
        // TODO: build a QueryRequest that fetches every item sharing this partition key,
        // regardless of sort key. Set .keyConditionExpression("category = :cat") and supply
        // ":cat" in .expressionAttributeValues(...) as a Map<String, AttributeValue>.
        QueryRequest request = null;

        QueryResponse response = client.query(request);
        return response.items();
    }

    private static void printItem(Map<String, AttributeValue> item) {
        if (item == null || item.isEmpty()) {
            System.out.println("  (not found)");
            return;
        }
        Map<String, String> readable = new HashMap<>();
        item.forEach((attrName, value) -> readable.put(attrName, value.s() != null ? value.s() : value.n()));
        System.out.println("  " + readable);
    }
}
