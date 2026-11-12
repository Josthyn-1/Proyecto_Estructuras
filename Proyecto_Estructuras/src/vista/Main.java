package vista;

import modelo.Libro;
import modelo.LibreriaRepositorio;
import prestamos.GestionPrestamos;
import seguridad.GestionUsuarios;
import seguridad.Rol;
import seguridad.Usuario;
import servicios.GestionLibreria;

import javax.swing.*;
import java.awt.Color;
import java.awt.Font;

/**
 * Punto de entrada del sistema de gestion de la librería.
 */
public class Main {

    public static void main(String[] args) {
        configurarAparienciaOscura();
        SwingUtilities.invokeLater(() -> {
            // Infraestructura compartida
            GestionUsuarios gestionUsuarios = new GestionUsuarios();
            LibreriaRepositorio repositorio = new LibreriaRepositorio();
            GestionLibreria gestionLibreria = new GestionLibreria();

            // Pasar los libros precargados del repositorio al gestionador de libreria
            for (Libro libro : repositorio.listarLibros()) {
                try {
                    gestionLibreria.agregarLibro(new Usuario("admin", "admin", Rol.ADMINISTRADOR), libro);
                } catch (Exception ignored) {
                    // Los libros ya fueron precargados internamente por LibreriaRepositorio;
                    // si se duplica, simplemente se ignora en este arranque inicial.
                }
            }

            GestionPrestamos gestionPrestamos = new GestionPrestamos(gestionLibreria);

            try {
                new ServidorWeb(gestionUsuarios, gestionLibreria, gestionPrestamos, new servicios.GestionClientes()).iniciar(8080);
                java.awt.Desktop.getDesktop().browse(java.net.URI.create("http://localhost:8080"));
                System.out.println("Jane Library lista en http://localhost:8080");
            } catch (Exception ex) {
                System.err.println("No se pudo iniciar el servidor web: " + ex.getMessage());
            }
        });
    }

    private static void configurarAparienciaOscura() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            System.err.println("No se pudo aplicar Nimbus; se usará la apariencia predeterminada.");
        }

        Color fondo = new Color(24, 24, 27);
        Color superficie = new Color(39, 39, 42);
        Color texto = new Color(244, 244, 245);

        UIManager.put("control", fondo);
        UIManager.put("info", superficie);
        UIManager.put("nimbusBase", fondo);
        UIManager.put("nimbusBlueGrey", new Color(63, 63, 70));
        UIManager.put("nimbusLightBackground", superficie);
        UIManager.put("text", texto);
        UIManager.put("Panel.background", fondo);
        UIManager.put("Label.foreground", texto);
        UIManager.put("Button.background", new Color(63, 63, 70));
        UIManager.put("Button.foreground", texto);
        UIManager.put("TextField.background", superficie);
        UIManager.put("TextField.foreground", texto);
        UIManager.put("PasswordField.background", superficie);
        UIManager.put("PasswordField.foreground", texto);
        UIManager.put("Table.background", superficie);
        UIManager.put("Table.foreground", texto);
        UIManager.put("TableHeader.background", new Color(39, 39, 42));
        UIManager.put("TableHeader.foreground", texto);
        UIManager.put("TabbedPane.background", fondo);
        UIManager.put("TabbedPane.foreground", texto);
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
    }
}
