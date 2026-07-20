package com.curriculum.labs;

/**
 * A tiny demo proving the service runs end to end with the fakes.
 * Run it with:  mvn -q compile exec:java
 *
 * This is NOT a test. It's here so you can see the moving parts move.
 */
public class Main {

    public static void main(String[] args) {
        OrderRepository repository = new InMemoryOrderRepository();
        PaymentGateway gateway = new FakePaymentGateway();
        OrderService service = new OrderService(repository, gateway);

        repository.save(new Order("A-1", "cust-42", 120.00, "OPEN"));

        Receipt receipt = service.checkout("A-1");
        System.out.println("receipt: " + receipt);
        System.out.println("order after checkout: " + repository.findById("A-1").orElseThrow());
    }
}
