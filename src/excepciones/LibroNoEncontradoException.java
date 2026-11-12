package excepciones;

/**
 * Excepción lanzada cuando no se encuentra un libro solicitado.
 */
public class LibroNoEncontradoException extends RuntimeException {

    public LibroNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public LibroNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
