package prestamos;

import excepciones.AccesoDenegadoException;
import excepciones.DatoInvalidoException;
import seguridad.Rol;
import seguridad.Usuario;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Representa un comprobante de cobro de mora generado por un Cajero o Administrador.
 * Asocia el ID del préstamo, el cliente y el monto cobrado.
 */
public class Factura {

    private static final AtomicLong SECUENCIA_IDS = new AtomicLong();

    private final String idFactura;
    private final String idPrestamo;
    private final String nombreCliente;
    private double monto;
    private final LocalDate fechaEmision;
    private final Usuario generadaPor;
    private boolean pagada;
    private boolean eliminada;
    private LocalDate fechaEliminacion;

    public Factura(String idPrestamo, String nombreCliente, double monto, Usuario generadaPor) {
        if (idPrestamo == null || idPrestamo.trim().isEmpty()) {
            throw new DatoInvalidoException("El ID del préstamo no puede ser nulo ni vacío.");
        }
        if (nombreCliente == null || nombreCliente.trim().isEmpty()) {
            throw new DatoInvalidoException("El nombre del cliente no puede ser nulo ni vacío.");
        }
        if (monto < 0) {
            throw new DatoInvalidoException("El monto de la factura no puede ser negativo.");
        }
        validarCajeroAdministrador(generadaPor);

        this.idFactura = String.format(Locale.ROOT, "FAC-%06d", SECUENCIA_IDS.incrementAndGet());
        this.idPrestamo = idPrestamo.trim();
        this.nombreCliente = nombreCliente.trim();
        this.monto = monto;
        this.fechaEmision = LocalDate.now();
        this.generadaPor = generadaPor;
        this.pagada = false;
        this.eliminada = false;
        this.fechaEliminacion = null;
    }

    private static void validarCajeroAdministrador(Usuario solicitante) {
        if (solicitante == null
                || (solicitante.getRol() != Rol.ADMINISTRADOR
                && solicitante.getRol() != Rol.CAJERO)) {
            throw new AccesoDenegadoException("Solo el Cajero o Administrador puede gestionar facturas.");
        }
    }

    public String getIdFactura() {
        return idFactura;
    }

    public String getIdPrestamo() {
        return idPrestamo;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public double getMonto() {
        return monto;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public Usuario getGeneradaPor() {
        return generadaPor;
    }

    public boolean isPagada() {
        return pagada;
    }

    public boolean isEliminada() {
        return eliminada;
    }

    public LocalDate getFechaEliminacion() {
        return fechaEliminacion;
    }

    public void modificar(Usuario solicitante, double monto, boolean pagada) {
        validarCajeroAdministrador(solicitante);
        if (eliminada) {
            throw new DatoInvalidoException("No se puede modificar una factura eliminada.");
        }
        if (Double.isNaN(monto) || Double.isInfinite(monto) || monto < 0) {
            throw new DatoInvalidoException("El monto debe ser un numero valido y no negativo.");
        }
        this.monto = monto;
        this.pagada = pagada;
    }

    public void marcarEliminada(Usuario solicitante) {
        validarCajeroAdministrador(solicitante);
        if (!eliminada) {
            this.eliminada = true;
            this.fechaEliminacion = LocalDate.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Factura)) return false;
        Factura factura = (Factura) o;
        return Objects.equals(idFactura, factura.idFactura);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFactura);
    }

    @Override
    public String toString() {
        return "Factura{idFactura='" + idFactura + "', idPrestamo='" + idPrestamo
            + "', cliente='" + nombreCliente + "', monto=" + monto
                + ", fechaEmision=" + fechaEmision
                + ", generadaPor=" + generadaPor.getUsername()
                + ", pagada=" + pagada + ", eliminada=" + eliminada + "}";
    }
}
