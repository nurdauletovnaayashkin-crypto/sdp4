package orderprocessing.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Cart {

    private final List<CartItem> items = new ArrayList<>();

    public Cart add(String sku, int quantity) {
        items.add(new CartItem(sku, quantity));
        return this;
    }

    public List<CartItem> items() {
        return Collections.unmodifiableList(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}