package excepciones;

/**
 * Excepción lanzada cuando se intenta registrar un nombre de usuario
 * que ya existe en el sistema.
 */
public class UsuarioDuplicadoException extends RuntimeException {

    public UsuarioDuplicadoException(String mensaje) {
        super(mensaje);
    }

    public UsuarioDuplicadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
