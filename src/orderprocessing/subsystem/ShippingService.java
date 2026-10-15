package orderprocessing.subsystem;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ShippingService {

    private final Set<String> supportedCountries = new HashSet<>();
    private final Set<String> activeShipments = new HashSet<>();
    private final AtomicInteger trackingCounter = new AtomicInteger(1);

    public static ShippingService withSampleData() {
        ShippingService shipping = new ShippingService();
        shipping.supportCountry("KZ");
        shipping.supportCountry("US");
        return shipping;
    }

    public void supportCountry(String countryCode) {
        supportedCountries.add(countryCode);
    }

    public String createShipment(String recipientName, String countryCode) {
        if (!supportedCountries.contains(countryCode)) {
            throw new ShippingException("Delivery to " + countryCode + " is not supported");
        }
        String trackingNumber = "TRK-" + trackingCounter.getAndIncrement();
        activeShipments.add(trackingNumber);
        System.out.println("[Shipping] Shipment " + trackingNumber + " created for " + recipientName);
        return trackingNumber;
    }

    public void cancelShipment(String trackingNumber) {
        if (!activeShipments.remove(trackingNumber)) {
            throw new ShippingException("No active shipment " + trackingNumber);
        }
        System.out.println("[Shipping] Shipment " + trackingNumber + " cancelled");
    }
}