package orderprocessing.facade;

import orderprocessing.model.Cart;
import orderprocessing.model.CartItem;
import orderprocessing.model.Customer;
import orderprocessing.subsystem.InventoryService;
import orderprocessing.subsystem.NotificationException;
import orderprocessing.subsystem.NotificationService;
import orderprocessing.subsystem.OutOfStockException;
import orderprocessing.subsystem.PaymentDeclinedException;
import orderprocessing.subsystem.PaymentProcessor;
import orderprocessing.subsystem.ProductCatalog;
import orderprocessing.subsystem.ShippingException;
import orderprocessing.subsystem.ShippingService;
import orderprocessing.subsystem.UnknownProductException;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderFacade {

    private final ProductCatalog catalog;
    private final InventoryService inventory;
    private final PaymentProcessor payments;
    private final ShippingService shipping;
    private final NotificationService notifications;

    private final Map<String, ProcessedOrder> activeOrders = new HashMap<>();
    private final AtomicInteger orderCounter = new AtomicInteger(1001);

    public OrderFacade(ProductCatalog catalog,
                       InventoryService inventory,
                       PaymentProcessor payments,
                       ShippingService shipping,
                       NotificationService notifications) {
        this.catalog = catalog;
        this.inventory = inventory;
        this.payments = payments;
        this.shipping = shipping;
        this.notifications = notifications;
    }

    public static OrderFacade createWithSampleData() {
        return new OrderFacade(
                ProductCatalog.withSampleData(),
                InventoryService.withSampleData(),
                PaymentProcessor.withSampleData(),
                ShippingService.withSampleData(),
                new NotificationService());
    }

    public OrderConfirmation placeOrder(Cart cart, Customer customer) {
        requireValid(cart, customer);

        Deque<Runnable> rollbackSteps = new ArrayDeque<>();
        try {
            BigDecimal total = calculateTotal(cart);

            String reservationId = inventory.reserve(cart.items());
            rollbackSteps.push(() -> inventory.release(reservationId));

            String paymentId = payments.charge(customer.id(), total);
            rollbackSteps.push(() -> payments.refund(paymentId));

            String trackingNumber = shipping.createShipment(customer.name(), customer.country());

            String orderId = "ORD-" + orderCounter.getAndIncrement();
            activeOrders.put(orderId,
                    new ProcessedOrder(orderId, customer, reservationId, paymentId, trackingNumber));

            notifyCustomer(customer, "Order " + orderId + " confirmed. Tracking: " + trackingNumber);
            return new OrderConfirmation(orderId, total, trackingNumber);

        } catch (UnknownProductException | OutOfStockException
                 | PaymentDeclinedException | ShippingException e) {
            rollBack(rollbackSteps);
            throw new OrderException("Order failed: " + e.getMessage(), e);
        }
    }

    public void cancelOrder(String orderId) {
        ProcessedOrder order = activeOrders.get(orderId);
        if (order == null) {
            throw new OrderException("Unknown or already cancelled order: " + orderId);
        }
        try {
            shipping.cancelShipment(order.trackingNumber());
        } catch (ShippingException e) {
            throw new OrderException("Order cannot be cancelled: " + e.getMessage(), e);
        }
        payments.refund(order.paymentId());
        inventory.release(order.reservationId());
        activeOrders.remove(orderId);

        notifyCustomer(order.customer(), "Order " + orderId + " was cancelled and refunded.");
    }

    private void requireValid(Cart cart, Customer customer) {
        if (customer == null) {
            throw new OrderException("Customer is required");
        }
        if (cart == null || cart.isEmpty()) {
            throw new OrderException("Cart is empty");
        }
    }

    private BigDecimal calculateTotal(Cart cart) {
        return cart.items().stream()
                .map(this::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal lineTotal(CartItem item) {
        return catalog.unitPriceOf(item.sku()).multiply(BigDecimal.valueOf(item.quantity()));
    }

    private void rollBack(Deque<Runnable> rollbackSteps) {
        rollbackSteps.forEach(Runnable::run);
    }

    private void notifyCustomer(Customer customer, String message) {
        try {
            notifications.send(customer.email(), message);
        } catch (NotificationException e) {
            System.out.println("[Facade] Warning: notification not delivered (" + e.getMessage() + ")");
        }
    }
}
