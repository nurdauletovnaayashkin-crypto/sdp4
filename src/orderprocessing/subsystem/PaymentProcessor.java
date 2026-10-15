package orderprocessing.subsystem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PaymentProcessor {

    private record Payment(String customerId, BigDecimal amount) {
    }

    private final Map<String, BigDecimal> balanceByCustomer = new HashMap<>();
    private final Map<String, Payment> payments = new HashMap<>();
    private final AtomicInteger paymentCounter = new AtomicInteger(1);

    public static PaymentProcessor withSampleData() {
        PaymentProcessor processor = new PaymentProcessor();
        processor.openAccount("C-1", new BigDecimal("2000.00"));
        processor.openAccount("C-2", new BigDecimal("50.00"));
        processor.openAccount("C-3", new BigDecimal("5000.00"));
        return processor;
    }

    public void openAccount(String customerId, BigDecimal balance) {
        balanceByCustomer.put(customerId, balance);
    }

    public String charge(String customerId, BigDecimal amount) {
        BigDecimal balance = balanceByCustomer.getOrDefault(customerId, BigDecimal.ZERO);
        if (balance.compareTo(amount) < 0) {
            throw new PaymentDeclinedException("Insufficient funds");
        }
        balanceByCustomer.put(customerId, balance.subtract(amount));

        String paymentId = "PAY-" + paymentCounter.getAndIncrement();
        payments.put(paymentId, new Payment(customerId, amount));
        System.out.println("[Payment] Charged " + amount + " (" + paymentId + ")");
        return paymentId;
    }

    public void refund(String paymentId) {
        Payment payment = payments.remove(paymentId);
        if (payment == null) {
            return;
        }
        balanceByCustomer.merge(payment.customerId(), payment.amount(), BigDecimal::add);
        System.out.println("[Payment] Refunded " + payment.amount() + " (" + paymentId + ")");
    }
}