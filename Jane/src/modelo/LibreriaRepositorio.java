package modelo;

import excepciones.DatoInvalidoException;
import excepciones.LibroDuplicadoException;
import excepciones.LibroNoEncontradoException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio en memoria de libros de la librería.
 * Se precarga con al menos 5 libros válidos al iniciarse.
 */
public class LibreriaRepositorio {

    private final List<Libro> libros;

    public LibreriaRepositorio() {
        this.libros = new ArrayList<>();
        precargarLibrosIniciales();
    }

    private void precargarLibrosIniciales() {
        libros.add(new Libro(1, "Cien años de soledad", "Gabriel García Márquez", 1,
                "Editorial Sudamericana", 1967));
        libros.add(new Libro(2, "Don Quijote de la Mancha", "Miguel de Cervantes", 2,
                "Real Academia Española", 1605));
        libros.add(new Libro(3, "1984", "George Orwell", 1,
                "Secker & Warburg", 1949));
        libros.add(new Libro(4, "El principito", "Antoine de Saint-Exupéry", 1,
                "Gallimard", 1943));
        libros.add(new Libro(5, "Rayuela", "Julio Cortázar", 1,
                "Ediciones Sudamericana", 1963));
    }

    /**
     * Agrega un libro al repositorio.
     *
     * @throws DatoInvalidoException      si el libro es nulo.
     * @throws LibroDuplicadoException  si ya existe un libro con el mismo ID.
     */
    public void agregarLibro(Libro libro) {
        if (libro == null) {
            throw new DatoInvalidoException("El libro no puede ser nulo.");
        }
        if (libros.stream().anyMatch(l -> l.getId() == libro.getId())) {
            throw new LibroDuplicadoException(
                    "Ya existe un libro con el ID " + libro.getId() + ".");
        }
        libros.add(libro);
    }

    /**
     * Busca un libro por su ID.
     *
     * @throws LibroNoEncontradoException si no existe un libro con ese ID.
     */
    public Libro buscarPorId(int id) {
        return libros.stream()
                .filter(l -> l.getId() == id)
                .findFirst()
                .orElseThrow(() -> new LibroNoEncontradoException(
                        "No se encontró un libro con el ID " + id + "."));
    }

    public List<Libro> listarLibros() {
        return new ArrayList<>(libros);
    }

    /**
     * Actualiza un libro existente identificado por su ID.
     *
     * @throws LibroNoEncontradoException si no existe un libro con ese ID.
     */
    public void actualizarLibro(Libro libro) {
        if (libro == null) {
            throw new DatoInvalidoException("El libro a actualizar no puede ser nulo.");
        }
        Libro existente = buscarPorId(libro.getId());
        int indice = libros.indexOf(existente);
        libros.set(indice, libro);
    }

    /**
     * Elimina un libro por su ID.
     *
     * @throws LibroNoEncontradoException si no existe un libro con ese ID.
     */
    public void eliminarLibro(int id) {
        Libro existente = buscarPorId(id);
        libros.remove(existente);
    }

    public int cantidadLibros() {
        return libros.size();
    }
}
