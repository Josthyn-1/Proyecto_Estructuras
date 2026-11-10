package excepciones;

/**
 * Excepción lanzada cuando un usuario intenta realizar una operación
 * para la que no tiene permisos según su rol.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }

    public AccesoDenegadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
