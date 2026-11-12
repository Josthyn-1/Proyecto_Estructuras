package excepciones;

/**
 * Excepción lanzada cuando se recibe un dato inválido:
 * campo vacío, nulo o fuera del rango lógico permitido.
 */
public class DatoInvalidoException extends RuntimeException {

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }

    public DatoInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
