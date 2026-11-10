package vista;

import modelo.Libro;
import modelo.LibreriaRepositorio;
import prestamos.GestionPrestamos;
import seguridad.GestionUsuarios;
import seguridad.Rol;
import seguridad.Usuario;
import servicios.GestionLibreria;

import javax.swing.*;

/**
 * Punto de entrada del sistema de gestion de la librería.
 */
public class Main {

    public static void main(String[] args) {
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

            LoginDialog loginDialog = new LoginDialog(gestionUsuarios);
            loginDialog.setLocationRelativeTo(null);
            loginDialog.setVisible(true);

            Usuario usuario = loginDialog.getUsuarioAutenticado();
            if (usuario != null) {
                MainWindow mainWindow = new MainWindow(
                        usuario, gestionUsuarios, gestionLibreria, gestionPrestamos);
                mainWindow.setLocationRelativeTo(null);
                mainWindow.setVisible(true);
            }
        });
    }
}
