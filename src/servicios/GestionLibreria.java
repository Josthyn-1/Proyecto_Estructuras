package servicios;

import excepciones.AccesoDenegadoException;
import excepciones.DatoInvalidoException;
import excepciones.LibroDuplicadoException;
import excepciones.LibroNoEncontradoException;
import estructuras.ArbolBinario;
import estructuras.TablaHash;
import modelo.Libro;
import seguridad.Rol;
import seguridad.Usuario;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Gestiona el catálogo de libros de la librería integrando dos estructuras
 * complementarias:
 * <ul>
 *   <li>{@link TablaHash}: indexa los libros por ID para obtener,
 *       actualizar y eliminar en tiempo constante O(1) promedio.</li>
 *   <li>{@link ArbolBinario}: mantiene los libros ordenados por ID y permite
 *       recorrer el catálogo en ese orden.</li>
 *   <li>{@link TreeMap}: indexa los libros por año de salida (Integer) para
 *       mantener el catálogo ordenado cronológicamente y permitir consultas
 *       por año o rangos temporales sin necesidad de reordenar.</li>
 * </ul>
 * Los índices se mantienen sincronizados en cada operación de escritura
 * para aprovechar las ventajas de cada una según el tipo de acceso requerido.
 */
public class GestionLibreria {

    // Índice hash para búsquedas rápidas e índice de árbol para el orden por ID.
    private final TablaHash<Integer, Libro> librosPorId;
    private final ArbolBinario<Libro> librosPorIdOrdenados;

    // Índice cronológico: año -> lista de libros publicados ese año.
    private final Map<Integer, List<Libro>> librosPorAnio;

    public GestionLibreria() {
        this.librosPorId = new TablaHash<>();
        this.librosPorIdOrdenados = new ArbolBinario<>(
                Comparator.comparingInt(Libro::getId));
        this.librosPorAnio = new TreeMap<>();
    }

    /* ==================== Utilidades internas ==================== */

    private void addAlIndiceAnio(Libro libro) {
        librosPorAnio.computeIfAbsent(libro.getAnioSalida(), k -> new ArrayList<>()).add(libro);
    }

    private void removeDelIndiceAnio(Libro libro) {
        List<Libro> lista = librosPorAnio.get(libro.getAnioSalida());
        if (lista != null) {
            lista.remove(libro);
            if (lista.isEmpty()) {
                librosPorAnio.remove(libro.getAnioSalida());
            }
        }
    }
    /**
     * Verifica que el solicitante tenga rol ADMINISTRADOR.
     *
     * @throws AccesoDenegadoException si el solicitante es nulo o no es ADMINISTRADOR.
     */
    private void validarAdministrador(Usuario solicitante) {
        if (solicitante == null || solicitante.getRol() != Rol.ADMINISTRADOR) {
            throw new AccesoDenegadoException(
                    "Acceso denegado: solo el rol ADMINISTRADOR puede realizar esta operación.");
        }
    }

    /**
     * Verifica que el solicitante esté autenticado (cualquier rol válido).
     *
     * @throws AccesoDenegadoException si no hay sesión activa.
     */
    private void validarAutenticado(Usuario solicitante) {
        if (solicitante == null) {
            throw new AccesoDenegadoException(
                    "Acceso denegado: se requiere autenticación para consultar el catálogo.");
        }
    }

    /* ==================== CRUD protegido por roles ==================== */

    /**
     * Agrega un libro al catálogo.
     *
     * @param solicitante usuario que ejecuta la operación (debe ser ADMINISTRADOR).
     * @param libro       libro a registrar.
     * @throws AccesoDenegadoException  si el solicitante no es ADMINISTRADOR.
     * @throws LibroDuplicadoException  si ya existe un libro con el mismo ID.
     * @throws DatoInvalidoException    si el libro es nulo o tiene datos inválidos.
     */
    public void agregarLibro(Usuario solicitante, Libro libro) {
        validarAdministrador(solicitante);

        if (libro == null) {
            throw new DatoInvalidoException("El libro no puede ser nulo.");
        }

        if (librosPorId.contieneClave(libro.getId())) {
            throw new LibroDuplicadoException(
                    "Ya existe un libro con el ID " + libro.getId() + ".");
        }

        // El constructor de Libro ya valida sus campos y lanza DatoInvalidoException si es necesario.
        librosPorId.insertar(libro.getId(), libro);
        librosPorIdOrdenados.insertar(libro);
        addAlIndiceAnio(libro);
    }

    /**
     * Busca un libro por su ID. Cualquier usuario autenticado puede consultar.
     *
     * @param solicitante usuario autenticado (ADMINISTRADOR o CAJERO).
     * @param id          ID del libro a buscar.
     * @return el libro encontrado.
     * @throws AccesoDenegadoException  si no hay sesión activa.
     * @throws LibroNoEncontradoException si no existe un libro con ese ID.
     */
    public Libro buscarLibro(Usuario solicitante, int id) {
        validarAutenticado(solicitante);

        Libro libro = librosPorId.obtener(id);
        if (libro == null) {
            throw new LibroNoEncontradoException(
                    "No se encontró un libro con el ID " + id + ".");
        }
        return libro;
    }

    /**
     * Actualiza un libro existente. Solo ADMINISTRADOR puede ejecutarlo.
     *
     * @param solicitante usuario que ejecuta la operación (debe ser ADMINISTRADOR).
     * @param libro       libro con los datos actualizados.
     * @throws AccesoDenegadoException  si el solicitante no es ADMINISTRADOR.
     * @throws LibroNoEncontradoException si no existe un libro con ese ID.
     * @throws DatoInvalidoException    si el libro es nulo o tiene datos inválidos.
     */
    public void actualizarLibro(Usuario solicitante, Libro libro) {
        validarAdministrador(solicitante);

        if (libro == null) {
            throw new DatoInvalidoException("El libro a actualizar no puede ser nulo.");
        }

        if (!librosPorId.contieneClave(libro.getId())) {
            throw new LibroNoEncontradoException(
                    "No se encontró un libro con el ID " + libro.getId() + " para actualizar.");
        }

        // Remover versión antigua del índice por año antes de sobrescribir.
        Libro anterior = librosPorId.obtener(libro.getId());
        removeDelIndiceAnio(anterior);

        librosPorId.insertar(libro.getId(), libro);
        librosPorIdOrdenados.eliminar(anterior);
        librosPorIdOrdenados.insertar(libro);
        addAlIndiceAnio(libro);
    }

    /**
     * Elimina un libro por su ID. Solo ADMINISTRADOR puede ejecutarlo.
     *
     * @param solicitante usuario que ejecuta la operación (debe ser ADMINISTRADOR).
     * @param id          ID del libro a eliminar.
     * @throws AccesoDenegadoException  si el solicitante no es ADMINISTRADOR.
     * @throws LibroNoEncontradoException si no existe un libro con ese ID.
     */
    public void eliminarLibro(Usuario solicitante, int id) {
        validarAdministrador(solicitante);

        Libro existente = librosPorId.obtener(id);
        if (existente == null) {
            throw new LibroNoEncontradoException(
                    "No se encontró un libro con el ID " + id + " para eliminar.");
        }

        librosPorId.eliminar(id);
        librosPorIdOrdenados.eliminar(existente);
        removeDelIndiceAnio(existente);
    }

    /* ==================== Consultas (ambos roles) ==================== */

    /**
     * Devuelve todos los libros del catálogo ordenados por ID mediante el árbol binario.
     *
     * @param solicitante usuario autenticado.
     * @return lista de libros.
     * @throws AccesoDenegadoException si no hay sesión activa.
     */
    public List<Libro> consultarCatalogo(Usuario solicitante) {
        validarAutenticado(solicitante);
        return librosPorIdOrdenados.recorridoInOrden();
    }

    /**
     * Devuelve los libros publicados en un año específico.
     *
     * @param solicitante usuario autenticado.
     * @param anio        año de salida.
     * @return lista de libros de ese año (vacía si no hay ninguno).
     * @throws AccesoDenegadoException si no hay sesión activa.
     */
    public List<Libro> consultarPorAnio(Usuario solicitante, int anio) {
        validarAutenticado(solicitante);
        List<Libro> lista = librosPorAnio.get(anio);
        return lista == null ? new ArrayList<>() : new ArrayList<>(lista);
    }

    /**
     * Devuelve el catálogo completo indexado por año (TreeMap ordenado cronológicamente).
     *
     * @param solicitante usuario autenticado.
     * @return mapa año -> lista de libros.
     * @throws AccesoDenegadoException si no hay sesión activa.
     */
    public Map<Integer, List<Libro>> consultarCatalogoCronologico(Usuario solicitante) {
        validarAutenticado(solicitante);
        // Devolver una copia superficial para evitar modificaciones externas al índice interno.
        Map<Integer, List<Libro>> copia = new TreeMap<>();
        for (Map.Entry<Integer, List<Libro>> entrada : librosPorAnio.entrySet()) {
            copia.put(entrada.getKey(), new ArrayList<>(entrada.getValue()));
        }
        return copia;
    }

    public int cantidadLibros() {
        return librosPorId.tamano();
    }
}
