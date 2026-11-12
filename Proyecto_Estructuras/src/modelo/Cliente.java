package modelo;

import excepciones.DatoInvalidoException;

/**
 * Datos de identificacion del cliente al que se registra un prestamo.
 */
public class Cliente {

    private final String cedula;
    private final String nombres;
    private final String apellidos;

    public Cliente(String cedula, String nombres, String apellidos) {
        if (cedula == null || !cedula.trim().matches("\\d{10}")) {
            throw new DatoInvalidoException("La cedula debe tener exactamente 10 digitos.");
        }
        if (nombres == null || nombres.trim().isEmpty()) {
            throw new DatoInvalidoException("Los nombres del cliente son obligatorios.");
        }
        if (apellidos == null || apellidos.trim().isEmpty()) {
            throw new DatoInvalidoException("Los apellidos del cliente son obligatorios.");
        }

        this.cedula = cedula.trim();
        this.nombres = nombres.trim();
        this.apellidos = apellidos.trim();
    }

    public String getCedula() {
        return cedula;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    private boolean bloqueado;
    private String motivoBloqueo = "";

    public boolean isBloqueado() {
        return bloqueado;
    }

    public String getMotivoBloqueo() {
        return motivoBloqueo;
    }

    public void bloquear(String motivo) {
        this.bloqueado = true;
        this.motivoBloqueo = (motivo == null || motivo.trim().isEmpty()) ? "Bloqueado manualmente." : motivo.trim();
    }

    public void desbloquear() {
        this.bloqueado = false;
        this.motivoBloqueo = "";
    }
}