# DynamoDB Query App (Java)

We'll create a DynamoDB table, then write a small Java app using the AWS SDK for Java 2.x that seeds it with sample data and queries it back two different ways: fetching one exact item by its full key, and fetching every item that shares a partition key. Those two access patterns are the heart of how DynamoDB is meant to be used, and this lab is about getting them under our fingers in real code — not just reading about them in the notes.

## Prerequisites

| Software | Required Version |
|---|---|
| JDK | 21 or later (LTS) |
| Maven | 3.9.x or later |
| AWS CLI v2 | 2.27 or later — used to create the table and configure credentials |
| AWS account credentials | Configured locally via `aws configure`, with permissions to manage DynamoDB tables |

The AWS SDK for Java dependency (`software.amazon.awssdk:dynamodb`, pinned via the SDK's BOM at version 2.49.6) is already wired up in `pom.xml` — Maven pulls it automatically, nothing to install by hand.

`cd` into this lab's directory before starting.

## Guided Walkthrough

### 1. Create the table

We're modeling a small product catalog: **`category`** as the partition key, **`productId`** as the sort key. That composite key is exactly the pattern from the DynamoDB notes — many products can share a category (partition key), while each individual product is still addressable on its own via its `productId` (sort key).

```bash
export TABLE_NAME=products-lab
export AWS_REGION=us-east-1

aws dynamodb create-table \
  --table-name $TABLE_NAME \
  --attribute-definitions \
      AttributeName=category,AttributeType=S \
      AttributeName=productId,AttributeType=S \
  --key-schema \
      AttributeName=category,KeyType=HASH \
      AttributeName=productId,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST \
  --region $AWS_REGION

aws dynamodb wait table-exists --table-name $TABLE_NAME --region $AWS_REGION
```
*`KeyType=HASH` is DynamoDB's term for the partition key, `KeyType=RANGE` for the sort key. `PAY_PER_REQUEST` is DynamoDB's on-demand billing mode — no throughput capacity to guess at or pre-provision, matching what the DynamoDB notes describe as one of the service's defining conveniences.*

### 2. Get oriented in `App.java`

Open `src/main/java/com/revature/labs/dynamodb/App.java`. `main()` is already wired up: it seeds five sample products (three `electronics`, two `books`), looks up one specific product, and then queries an entire category. Three private methods underneath are where the actual DynamoDB calls happen — and each currently has a `TODO` in place of real code. We'll fill them in one at a time.

### 3. Write a single item (`putProduct`)

Find this in `putProduct`:

```java
// TODO: build the item map DynamoDB expects...
Map<String, AttributeValue> item = null;
```

DynamoDB items are just maps from attribute name to a typed `AttributeValue`. Let's replace that line with:

```java
Map<String, AttributeValue> item = new HashMap<>();
item.put("category", AttributeValue.builder().s(product.category()).build());
item.put("productId", AttributeValue.builder().s(product.productId()).build());
item.put("name", AttributeValue.builder().s(product.name()).build());
item.put("price", AttributeValue.builder().n(product.price()).build());
```
*`.s(...)` marks a string-typed attribute, `.n(...)` marks a number-typed one (still passed in as a `String` — the SDK and DynamoDB itself handle the numeric representation). Notice we're free to put whatever attributes we want here; nothing about the table definition constrains this beyond the two key attributes.*

### 4. Fetch one exact item (`getProduct`)

Find the TODO in `getProduct`:

```java
// TODO: build the key map for a GetItem request...
Map<String, AttributeValue> key = null;
```

A `GetItem` key only needs the key attributes — not the whole item:

```java
Map<String, AttributeValue> key = new HashMap<>();
key.put("category", AttributeValue.builder().s(category).build());
key.put("productId", AttributeValue.builder().s(productId).build());
```
*This is the fastest, cheapest kind of read DynamoDB offers — an exact lookup by full primary key, the "given this ID, fetch that record" pattern called out in the DynamoDB notes.*

### 5. Query an entire partition (`queryCategory`)

Find the TODO in `queryCategory`:

```java
// TODO: build a QueryRequest that fetches every item sharing this partition key...
QueryRequest request = null;
```

Replace it with:

```java
Map<String, AttributeValue> expressionValues = new HashMap<>();
expressionValues.put(":cat", AttributeValue.builder().s(category).build());

QueryRequest request = QueryRequest.builder()
        .tableName(tableName)
        .keyConditionExpression("category = :cat")
        .expressionAttributeValues(expressionValues)
        .build();
```
*Unlike `getProduct`, this only pins down the partition key — no sort key given — so it returns every item under that partition (all three `electronics` products), not just one. This is the other half of DynamoDB's core access pattern: not "give me this one record," but "give me every record filed under this key."*

### 6. Run it

```bash
mvn -q exec:java
```
*Runs `App.main()`. You should see confirmation that five products were seeded, one exact product printed from the `getProduct` lookup, and three products printed from the `queryCategory` call — everything filed under `electronics`.*

If you re-run it, that's fine — `putItem` calls with the same key overwrite the existing item rather than erroring, so seeding is safe to repeat.

### 7. Clean up

```bash
aws dynamodb delete-table --table-name $TABLE_NAME --region $AWS_REGION
```

## Exercises

Keep the table around from the walkthrough (or recreate it) for these.

1. **Update in place.** Add a new method, `updatePrice`, that uses `client.updateItem(...)` to change the `price` attribute on one existing product (look at how `UpdateItemRequest` is built — it needs a `key`, similar to `getProduct`, plus an update expression). Call it from `main()`, then call `getProduct` again on that same item and confirm the new price comes back.

2. **Delete and re-query.** Add a `deleteProduct` method using `client.deleteItem(...)`, remove one of the `electronics` products, and run `queryCategory` again — confirm the result set has shrunk from three items to two.

3. **A product with an extra attribute.** Add a sixth sample product to `SAMPLE_PRODUCTS` — in the `books` category — but give it an attribute none of the others have (an `author` field, say). You'll need to adjust how it's put into the item map, since our current `Product` record and `putProduct` method don't have a slot for it. Query the `books` category and confirm your new item comes back correctly alongside the others, even though it has a different shape. This is the schema flexibility from the DynamoDB notes made concrete: nothing about the table enforces that every item in it looks the same.
