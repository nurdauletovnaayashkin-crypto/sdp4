package orderprocessing;

import orderprocessing.facade.OrderConfirmation;
import orderprocessing.facade.OrderException;
import orderprocessing.facade.OrderFacade;
import orderprocessing.model.Cart;
import orderprocessing.model.Customer;

public class Main {

    public static void main(String[] args) {
        OrderFacade orders = OrderFacade.createWithSampleData();

        Customer aigerim = new Customer("C-1", "Aigerim", "aigerim@mail.kz", "KZ");
        Customer dana = new Customer("C-2", "Dana", "dana@mail.kz", "KZ");
        Customer john = new Customer("C-3", "John", "john@mail.com", "FR");

        printTitle("1. Successful order");
        OrderConfirmation confirmation =
                orders.placeOrder(new Cart().add("LAPTOP", 1).add("MOUSE", 2), aigerim);
        System.out.println("Result: " + confirmation);

        printTitle("2. Payment declined -> stock is released");
        tryPlaceOrder(orders, new Cart().add("MONITOR", 1), dana);

        printTitle("3. Shipping unsupported -> payment refunded and stock released");
        tryPlaceOrder(orders, new Cart().add("MONITOR", 1), john);

        printTitle("4. Not enough stock");
        tryPlaceOrder(orders, new Cart().add("MONITOR", 5), aigerim);

        printTitle("5. Cancel the first order");
        orders.cancelOrder(confirmation.orderId());

        printTitle("6. Cancel the same order again");
        try {
            orders.cancelOrder(confirmation.orderId());
        } catch (OrderException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }

    private static void tryPlaceOrder(OrderFacade orders, Cart cart, Customer customer) {
        try {
            orders.placeOrder(cart, customer);
        } catch (OrderException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }

    private static void printTitle(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}