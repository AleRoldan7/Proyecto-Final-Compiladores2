package exceptiones;

public class RegistroException extends RuntimeException{

    public RegistroException() {
    }

    public RegistroException(String message) {
        super(message);
    }
}
