package orderprocessing.facade;

public class OrderException extends RuntimeException {

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderException(String message) {
        super(message);
    }
}