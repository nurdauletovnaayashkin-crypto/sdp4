package orderprocessing.subsystem;

public class UnknownProductException extends RuntimeException {

    public UnknownProductException(String message) {
        super(message);
    }
}