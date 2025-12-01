package main.java.excepciones;

public class TransaccionException extends Exception {
    public TransaccionException(String mensaje) {
        super(mensaje);
    }
}
