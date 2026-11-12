package excepciones;

/**
 * Excepción lanzada cuando se intenta registrar un libro con un ID que ya existe.
 */
public class LibroDuplicadoException extends RuntimeException {

    public LibroDuplicadoException(String mensaje) {
        super(mensaje);
    }

    public LibroDuplicadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
