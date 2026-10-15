package orderprocessing.facade;

import java.math.BigDecimal;

public record OrderConfirmation(String orderId, BigDecimal total, String trackingNumber) {
}