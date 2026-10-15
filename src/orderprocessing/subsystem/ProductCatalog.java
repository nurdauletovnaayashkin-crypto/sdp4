package orderprocessing.subsystem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ProductCatalog {

    private final Map<String, BigDecimal> pricesBySku = new HashMap<>();

    public static ProductCatalog withSampleData() {
        ProductCatalog catalog = new ProductCatalog();
        catalog.addProduct("LAPTOP", new BigDecimal("900.00"));
        catalog.addProduct("MOUSE", new BigDecimal("25.00"));
        catalog.addProduct("MONITOR", new BigDecimal("300.00"));
        return catalog;
    }

    public void addProduct(String sku, BigDecimal unitPrice) {
        pricesBySku.put(sku, unitPrice);
    }

    public BigDecimal unitPriceOf(String sku) {
        BigDecimal price = pricesBySku.get(sku);
        if (price == null) {
            throw new UnknownProductException("Unknown product: " + sku);
        }
        return price;
    }
}