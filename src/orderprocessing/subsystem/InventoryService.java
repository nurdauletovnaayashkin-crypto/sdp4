package orderprocessing.subsystem;

import orderprocessing.model.CartItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InventoryService {

    private final Map<String, Integer> stockBySku = new HashMap<>();
    private final Map<String, List<CartItem>> reservations = new HashMap<>();
    private final AtomicInteger reservationCounter = new AtomicInteger(1);

    public static InventoryService withSampleData() {
        InventoryService inventory = new InventoryService();
        inventory.addStock("LAPTOP", 5);
        inventory.addStock("MOUSE", 50);
        inventory.addStock("MONITOR", 2);
        return inventory;
    }

    public void addStock(String sku, int quantity) {
        stockBySku.merge(sku, quantity, Integer::sum);
    }

    public int availableStock(String sku) {
        return stockBySku.getOrDefault(sku, 0);
    }

    public String reserve(List<CartItem> items) {
        items.forEach(this::requireEnoughStock);
        items.forEach(item -> stockBySku.merge(item.sku(), -item.quantity(), Integer::sum));

        String reservationId = "RES-" + reservationCounter.getAndIncrement();
        reservations.put(reservationId, List.copyOf(items));
        System.out.println("[Inventory] Reserved stock " + reservationId);
        return reservationId;
    }

    public void release(String reservationId) {
        List<CartItem> items = reservations.remove(reservationId);
        if (items == null) {
            return;
        }
        items.forEach(item -> stockBySku.merge(item.sku(), item.quantity(), Integer::sum));
        System.out.println("[Inventory] Released stock " + reservationId);
    }

    private void requireEnoughStock(CartItem item) {
        if (availableStock(item.sku()) < item.quantity()) {
            throw new OutOfStockException("Not enough stock for " + item.sku());
        }
    }
}