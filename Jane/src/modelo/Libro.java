package modelo;

import excepciones.DatoInvalidoException;
import java.time.Year;
import java.util.Objects;

/**
 * Representa un libro de la librería.
 * Todos sus atributos son validados al construirse y al modificarse;
 * cualquier dato inválido lanza DatoInvalidoException.
 */
public class Libro {

    private final int id;
    private String nombreLibro;
    private String autor;
    private int edicion;
    private String editorial;
    private int anioSalida;

    public Libro(int id, String nombreLibro, String autor, int edicion,
                 String editorial, int anioSalida) {
        validarId(id);
        validarNombreLibro(nombreLibro);
        validarAutor(autor);
        validarEdicion(edicion);
        validarEditorial(editorial);
        validarAnioSalida(anioSalida);

        this.id = id;
        this.nombreLibro = nombreLibro.trim();
        this.autor = autor.trim();
        this.edicion = edicion;
        this.editorial = editorial.trim();
        this.anioSalida = anioSalida;
    }

    /* -------------------- Validaciones -------------------- */

    private void validarId(int id) {
        if (id <= 0) {
            throw new DatoInvalidoException("El ID del libro debe ser mayor a cero.");
        }
    }

    private void validarNombreLibro(String nombreLibro) {
        if (nombreLibro == null || nombreLibro.trim().isEmpty()) {
            throw new DatoInvalidoException("El nombre del libro no puede ser nulo ni vacío.");
        }
    }

    private void validarAutor(String autor) {
        if (autor == null || autor.trim().isEmpty()) {
            throw new DatoInvalidoException("El autor no puede ser nulo ni vacío.");
        }
    }

    private void validarEdicion(int edicion) {
        if (edicion <= 0) {
            throw new DatoInvalidoException("La edición debe ser mayor a cero.");
        }
    }

    private void validarEditorial(String editorial) {
        if (editorial == null || editorial.trim().isEmpty()) {
            throw new DatoInvalidoException("La editorial no puede ser nula ni vacía.");
        }
    }

    private void validarAnioSalida(int anioSalida) {
        int anioActual = Year.now().getValue();
        if (anioSalida < 1000 || anioSalida > anioActual) {
            throw new DatoInvalidoException(
                    "El año de salida debe estar entre 1000 y " + anioActual + ".");
        }
    }

    /* -------------------- Getters y Setters -------------------- */

    public int getId() {
        return id;
    }

    public String getNombreLibro() {
        return nombreLibro;
    }

    public void setNombreLibro(String nombreLibro) {
        validarNombreLibro(nombreLibro);
        this.nombreLibro = nombreLibro.trim();
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        validarAutor(autor);
        this.autor = autor.trim();
    }

    public int getEdicion() {
        return edicion;
    }

    public void setEdicion(int edicion) {
        validarEdicion(edicion);
        this.edicion = edicion;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        validarEditorial(editorial);
        this.editorial = editorial.trim();
    }

    public int getAnioSalida() {
        return anioSalida;
    }

    public void setAnioSalida(int anioSalida) {
        validarAnioSalida(anioSalida);
        this.anioSalida = anioSalida;
    }

    /* -------------------- Object overrides -------------------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Libro)) return false;
        Libro libro = (Libro) o;
        return id == libro.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Libro{id=" + id + ", nombreLibro='" + nombreLibro + "', autor='" + autor
                + "', edicion=" + edicion + ", editorial='" + editorial
                + "', anioSalida=" + anioSalida + "}";
    }
}
