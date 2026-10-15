package orderprocessing.facade;

import orderprocessing.model.Customer;

record ProcessedOrder(
        String orderId,
        Customer customer,
        String reservationId,
        String paymentId,
        String trackingNumber) {
}