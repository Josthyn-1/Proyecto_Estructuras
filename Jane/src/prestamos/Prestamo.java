package prestamos;

import excepciones.DatoInvalidoException;
import modelo.Cliente;
import modelo.Libro;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa un préstamo de libro en la librería.
 * Registra los datos del usuario, el libro prestado y las fechas de entrega
 * estimada y real. Inicia como activo y se desactiva al devolverse.
 */
public class Prestamo {

    private final String idPrestamo;
    private final Libro libroPrestado;
    private final Cliente cliente;
    private final LocalDate fechaPrestamo;
    private final LocalDate fechaDevolucionEstimada;
    private LocalDate fechaDevolucionReal;
    private boolean activo;

    public Prestamo(String idPrestamo, Libro libroPrestado, Cliente cliente,
                    LocalDate fechaPrestamo, LocalDate fechaDevolucionEstimada) {
        if (idPrestamo == null || idPrestamo.trim().isEmpty()) {
            throw new DatoInvalidoException("El ID del préstamo no puede ser nulo ni vacío.");
        }
        if (libroPrestado == null) {
            throw new DatoInvalidoException("El libro prestado no puede ser nulo.");
        }
        if (cliente == null) {
            throw new DatoInvalidoException("El cliente del préstamo no puede ser nulo.");
        }
        if (fechaPrestamo == null) {
            throw new DatoInvalidoException("La fecha de préstamo no puede ser nula.");
        }
        if (fechaDevolucionEstimada == null) {
            throw new DatoInvalidoException("La fecha de devolución estimada no puede ser nula.");
        }
        if (fechaDevolucionEstimada.isBefore(fechaPrestamo)) {
            throw new DatoInvalidoException(
                    "La fecha de devolución estimada no puede ser anterior a la fecha de préstamo.");
        }

        this.idPrestamo = idPrestamo.trim();
        this.libroPrestado = libroPrestado;
        this.cliente = cliente;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionEstimada = fechaDevolucionEstimada;
        this.fechaDevolucionReal = null;
        this.activo = true;
    }

    public String getIdPrestamo() {
        return idPrestamo;
    }

    public Libro getLibroPrestado() {
        return libroPrestado;
    }

    public String getNombreUsuario() {
        return cliente.getNombreCompleto();
    }

    public Cliente getCliente() {
        return cliente;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public LocalDate getFechaDevolucionEstimada() {
        return fechaDevolucionEstimada;
    }

    public LocalDate getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    public void setFechaDevolucionReal(LocalDate fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prestamo)) return false;
        Prestamo prestamo = (Prestamo) o;
        return Objects.equals(idPrestamo, prestamo.idPrestamo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPrestamo);
    }

    @Override
    public String toString() {
        return "Prestamo{idPrestamo='" + idPrestamo + "', libro=" + libroPrestado.getId()
            + ", cliente='" + cliente.getNombreCompleto() + "', cedula='" + cliente.getCedula()
            + "', fechaPrestamo=" + fechaPrestamo
                + ", fechaEstimada=" + fechaDevolucionEstimada
                + ", fechaReal=" + fechaDevolucionReal + ", activo=" + activo + "}";
    }
}
