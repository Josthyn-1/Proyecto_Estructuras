package modelo;

import excepciones.DatoInvalidoException;
import excepciones.LibroDuplicadoException;
import excepciones.LibroNoEncontradoException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio en memoria de libros de la librería.
 * Se precarga con 25 libros válidos al iniciarse.
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
        libros.add(new Libro(6, "La casa de los espíritus", "Isabel Allende", 1,
                "Plaza & Janés", 1982));
        libros.add(new Libro(7, "Ficciones", "Jorge Luis Borges", 1,
                "Editorial Sur", 1944));
        libros.add(new Libro(8, "La ciudad y los perros", "Mario Vargas Llosa", 1,
                "Seix Barral", 1963));
        libros.add(new Libro(9, "Pedro Páramo", "Juan Rulfo", 1,
                "Fondo de Cultura Económica", 1955));
        libros.add(new Libro(10, "La sombra del viento", "Carlos Ruiz Zafón", 1,
                "Editorial Planeta", 2001));
        libros.add(new Libro(11, "El amor en los tiempos del cólera", "Gabriel García Márquez", 1,
                "Editorial Oveja Negra", 1985));
        libros.add(new Libro(12, "Crimen y castigo", "Fiódor Dostoyevski", 1,
                "Editorial Alba", 1866));
        libros.add(new Libro(13, "Orgullo y prejuicio", "Jane Austen", 1,
                "T. Egerton", 1813));
        libros.add(new Libro(14, "Moby Dick", "Herman Melville", 1,
                "Harper & Brothers", 1851));
        libros.add(new Libro(15, "Frankenstein", "Mary Shelley", 1,
                "Lackington, Hughes, Harding, Mavor & Jones", 1818));
        libros.add(new Libro(16, "Drácula", "Bram Stoker", 1,
                "Archibald Constable and Company", 1897));
        libros.add(new Libro(17, "Alicia en el país de las maravillas", "Lewis Carroll", 1,
                "Macmillan", 1865));
        libros.add(new Libro(18, "Mujercitas", "Louisa May Alcott", 1,
                "Roberts Brothers", 1868));
        libros.add(new Libro(19, "El viejo y el mar", "Ernest Hemingway", 1,
                "Charles Scribner's Sons", 1952));
        libros.add(new Libro(20, "El gran Gatsby", "F. Scott Fitzgerald", 1,
                "Charles Scribner's Sons", 1925));
        libros.add(new Libro(21, "La metamorfosis", "Franz Kafka", 1,
                "Kurt Wolff Verlag", 1915));
        libros.add(new Libro(22, "El túnel", "Ernesto Sabato", 1,
                "Editorial Sur", 1948));
        libros.add(new Libro(23, "La tregua", "Mario Benedetti", 1,
                "Editorial Alfa", 1960));
        libros.add(new Libro(24, "Los miserables", "Victor Hugo", 1,
                "A. Lacroix, Verboeckhoven & Cie.", 1862));
        libros.add(new Libro(25, "Ensayo sobre la ceguera", "José Saramago", 1,
                "Editorial Caminho", 1995));
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
