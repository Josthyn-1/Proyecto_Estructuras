package prestamos;

import excepciones.AccesoDenegadoException;
import excepciones.DatoInvalidoException;
import excepciones.LibroNoEncontradoException;
import modelo.Cliente;
import modelo.Libro;
import seguridad.Rol;
import seguridad.Usuario;
import servicios.GestionLibreria;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestiona los préstamos de libros, las devoluciones y el cálculo de multas.
 * Es accesible tanto para Cajeros como para Administradores.
 * <p>
 * Se apoya en {@link GestionLibreria} para validar la existencia del libro
 * y en {@link Factura} para generar el comprobante de cobro de la multa.
 */
public class GestionPrestamos {

    private static final double COSTO_FIJO_POR_DIA_RETRASO = 2000.0;

    private final GestionLibreria libreria;
    private final Map<String, Prestamo> prestamos;
    private final Map<Integer, String> librosPrestados; // idLibro -> idPrestamo
    private final List<Factura> facturas;

    public GestionPrestamos(GestionLibreria libreria) {
        this.libreria = libreria;
        this.prestamos = new HashMap<>();
        this.librosPrestados = new HashMap<>();
        this.facturas = new ArrayList<>();
    }

    /* ==================== Validación de roles ==================== */

    private void validarCajeroAdministrador(Usuario solicitante) {
        if (solicitante == null
                || (solicitante.getRol() != Rol.ADMINISTRADOR
                && solicitante.getRol() != Rol.CAJERO)) {
            throw new AccesoDenegadoException(
                    "Acceso denegado: solo el Cajero o el Administrador pueden gestionar préstamos.");
        }
    }

    /* ==================== Registro de préstamo ==================== */

    /**
     * Registra un nuevo préstamo.
     * Valida que el libro exista en el catálogo y que no esté ya prestado.
     *
     * @throws AccesoDenegadoException  si el solicitante no es Cajero ni Administrador.
     * @throws DatoInvalidoException    si los datos son inválidos, el préstamo ya existe
     *                                  o el libro no está disponible.
     * @throws LibroNoEncontradoException si el libro no existe en el catálogo.
     */
    public Prestamo registrarPrestamo(Usuario solicitante, String idPrestamo, int idLibro,
                                      Cliente cliente, LocalDate fechaPrestamo,
                                      LocalDate fechaDevolucionEstimada) {
        validarCajeroAdministrador(solicitante);

        if (idPrestamo == null || idPrestamo.trim().isEmpty()) {
            throw new DatoInvalidoException("El ID del préstamo no puede estar vacío.");
        }
        if (prestamos.containsKey(idPrestamo.trim())) {
            throw new DatoInvalidoException("Ya existe un préstamo con el ID " + idPrestamo + ".");
        }
        if (cliente == null) {
            throw new DatoInvalidoException("El cliente del préstamo no puede estar vacío.");
        }
        if (cliente.isBloqueado()) {
            throw new DatoInvalidoException(
                    "El cliente tiene prohibido el préstamo de libros: " + cliente.getMotivoBloqueo());
        }
        if (fechaPrestamo == null || fechaDevolucionEstimada == null) {
            throw new DatoInvalidoException("Las fechas de préstamo y estimada no pueden ser nulas.");
        }
        if (fechaDevolucionEstimada.isBefore(fechaPrestamo)) {
            throw new DatoInvalidoException(
                    "La fecha de devolución estimada no puede ser anterior a la fecha de préstamo.");
        }

        // Validar existencia del libro en el catálogo (acceso de consulta permitido a ambos roles)
        Libro libro = libreria.buscarLibro(solicitante, idLibro);

        if (librosPrestados.containsKey(idLibro)) {
            throw new DatoInvalidoException(
                    "El libro con ID " + idLibro + " ya está prestado y no está disponible.");
        }

        Prestamo prestamo = new Prestamo(idPrestamo.trim(), libro, cliente,
                fechaPrestamo, fechaDevolucionEstimada);
        prestamos.put(prestamo.getIdPrestamo(), prestamo);
        librosPrestados.put(idLibro, prestamo.getIdPrestamo());
        return prestamo;
    }

    /* ==================== Devolución y multas ==================== */

    /**
     * Registra la devolución de un préstamo, establece la fecha real de devolución
     * y calcula la multa aplicable por retraso.
     *
     * @return el monto de la multa calculada (0 si no hay retraso).
     * @throws AccesoDenegadoException  si el solicitante no es Cajero ni Administrador.
     * @throws DatoInvalidoException    si el préstamo no existe o ya fue devuelto.
     */
    public double registrarDevolucion(Usuario solicitante, String idPrestamo) {
        return registrarDevolucion(solicitante, idPrestamo, LocalDate.now());
    }

    public double registrarDevolucion(Usuario solicitante, String idPrestamo, LocalDate fechaReal) {
        validarCajeroAdministrador(solicitante);

        Prestamo prestamo = prestamos.get(idPrestamo);
        if (prestamo == null) {
            throw new DatoInvalidoException("No se encontró un préstamo con el ID " + idPrestamo + ".");
        }
        if (!prestamo.isActivo()) {
            throw new DatoInvalidoException(
                    "El préstamo " + idPrestamo + " ya fue devuelto previamente.");
        }

        if (fechaReal == null || fechaReal.isBefore(prestamo.getFechaPrestamo())) {
            throw new DatoInvalidoException("La fecha de devolución no puede ser anterior a la fecha del préstamo.");
        }
        prestamo.setFechaDevolucionReal(fechaReal);
        prestamo.setActivo(false);
        librosPrestados.remove(prestamo.getLibroPrestado().getId());

        double multa = calcularMulta(prestamo);
        if (multa > 0) {
            prestamo.getCliente().bloquear("Devolvió con retraso el préstamo " + prestamo.getIdPrestamo()
                    + " (mora de $" + (long) multa + ").");
        }
        return multa;
    }

    /**
     * Calcula la multa comparando la fecha real (o actual si aún no se devuelve)
     * con la fecha estimada. Aplica un costo fijo por cada día de retraso.
     *
     * @return monto de la multa; 0 si no hay retraso.
     * @throws DatoInvalidoException si el préstamo es nulo.
     */
    public double calcularMulta(Prestamo prestamo) {
        if (prestamo == null) {
            throw new DatoInvalidoException("El préstamo no puede ser nulo.");
        }

        LocalDate fechaReal = prestamo.getFechaDevolucionReal() != null
                ? prestamo.getFechaDevolucionReal()
                : LocalDate.now();

        LocalDate fechaEstimada = prestamo.getFechaDevolucionEstimada();

        if (fechaReal.isAfter(fechaEstimada)) {
            long diasRetraso = ChronoUnit.DAYS.between(fechaEstimada, fechaReal);
            return diasRetraso * COSTO_FIJO_POR_DIA_RETRASO;
        }
        return 0.0;
    }

    public Factura generarFactura(Usuario solicitante, String idPrestamo) {
        validarCajeroAdministrador(solicitante);
        Prestamo prestamo = buscarPrestamo(solicitante, idPrestamo);
        if (prestamo.isActivo()) {
            throw new DatoInvalidoException(
                    "El libro debe ser devuelto antes de generar la factura de mora.");
        }
        for (Factura factura : facturas) {
            if (!factura.isEliminada() && factura.getIdPrestamo().equals(idPrestamo)) {
                throw new DatoInvalidoException("Ya existe una factura para este prestamo.");
            }
        }

        double monto = calcularMulta(prestamo);
        if (monto <= 0) {
            throw new DatoInvalidoException("El prestamo no tiene mora pendiente.");
        }

        Factura factura = new Factura(
                idPrestamo, prestamo.getCliente().getNombreCompleto(), monto, solicitante);
        facturas.add(factura);
        return factura;
    }

    public void modificarFactura(Usuario solicitante, String idFactura, double monto, boolean pagada) {
        validarCajeroAdministrador(solicitante);
        for (Factura factura : facturas) {
            if (factura.getIdFactura().equals(idFactura) && !factura.isEliminada()) {
                factura.modificar(solicitante, monto, pagada);
                return;
            }
        }
        throw new DatoInvalidoException("No se encontro la factura " + idFactura + ".");
    }

    public List<Factura> listarFacturas(Usuario solicitante) {
        validarCajeroAdministrador(solicitante);
        List<Factura> vigentes = new ArrayList<>();
        for (Factura factura : facturas) {
            if (!factura.isEliminada()) {
                vigentes.add(factura);
            }
        }
        return vigentes;
    }

    /**
     * Lista préstamos devueltos que tienen mora y aún no tienen una factura vigente.
     *
     * @throws AccesoDenegadoException si el solicitante no es Cajero ni Administrador.
     */
    public List<Prestamo> listarPrestamosConMoraPendiente(Usuario solicitante) {
        validarCajeroAdministrador(solicitante);
        List<Prestamo> pendientes = new ArrayList<>();
        for (Prestamo prestamo : prestamos.values()) {
            if (prestamo.isActivo() || calcularMulta(prestamo) <= 0) {
                continue;
            }

            boolean facturaVigenteExiste = facturas.stream()
                    .anyMatch(factura -> !factura.isEliminada()
                            && factura.getIdPrestamo().equals(prestamo.getIdPrestamo()));
            if (!facturaVigenteExiste) {
                pendientes.add(prestamo);
            }
        }
        pendientes.sort(Comparator.comparing(Prestamo::getFechaDevolucionReal).reversed());
        return pendientes;
    }

    public List<Factura> listarHistorialFacturas(Usuario solicitante) {
        validarCajeroAdministrador(solicitante);
        return new ArrayList<>(facturas);
    }

    public void eliminarFactura(Usuario solicitante, String idFactura) {
        validarCajeroAdministrador(solicitante);
        for (Factura factura : facturas) {
            if (factura.getIdFactura().equals(idFactura) && !factura.isEliminada()) {
                factura.marcarEliminada(solicitante);
                return;
            }
        }
        throw new DatoInvalidoException("No se encontro la factura " + idFactura + ".");
    }

    /* ==================== Consultas ==================== */

    /**
     * Devuelve la lista de préstamos activos (aún no devueltos).
     *
     * @throws AccesoDenegadoException si el solicitante no es Cajero ni Administrador.
     */
    public List<Prestamo> listarPrestamosActivos(Usuario solicitante) {
        validarCajeroAdministrador(solicitante);
        List<Prestamo> activos = new ArrayList<>();
        for (Prestamo p : prestamos.values()) {
            if (p.isActivo()) {
                activos.add(p);
            }
        }
        return activos;
    }

    /**
     * Devuelve los libros del catálogo que no están prestados actualmente.
     *
     * @throws AccesoDenegadoException si el solicitante no es Cajero ni Administrador.
     */
    public List<Libro> listarLibrosDisponibles(Usuario solicitante) {
        validarCajeroAdministrador(solicitante);
        List<Libro> disponibles = new ArrayList<>();
        for (Libro libro : libreria.consultarCatalogo(solicitante)) {
            if (!librosPrestados.containsKey(libro.getId())) {
                disponibles.add(libro);
            }
        }
        return disponibles;
    }

    public List<Prestamo> listarHistorialPrestamos(Usuario solicitante) {
        validarCajeroAdministrador(solicitante);
        List<Prestamo> historial = new ArrayList<>(prestamos.values());
        historial.sort(Comparator.comparing(Prestamo::getFechaPrestamo).reversed()
                .thenComparing(Prestamo::getIdPrestamo));
        return historial;
    }

    /**
     * Busca un préstamo por su ID.
     *
     * @throws AccesoDenegadoException  si el solicitante no es Cajero ni Administrador.
     * @throws DatoInvalidoException    si no se encuentra el préstamo.
     */
    public Prestamo buscarPrestamo(Usuario solicitante, String idPrestamo) {
        validarCajeroAdministrador(solicitante);
        Prestamo prestamo = prestamos.get(idPrestamo);
        if (prestamo == null) {
            throw new DatoInvalidoException("No se encontró el préstamo con ID " + idPrestamo + ".");
        }
        return prestamo;
    }

    public double getCostoFijoPorDiaRetraso() {
        return COSTO_FIJO_POR_DIA_RETRASO;
    }
}

