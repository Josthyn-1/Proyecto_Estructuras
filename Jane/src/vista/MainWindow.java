package vista;

import prestamos.GestionPrestamos;
import seguridad.GestionUsuarios;
import seguridad.Rol;
import seguridad.Usuario;
import servicios.GestionLibreria;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal con pestañas (JTabbedPane).
 * La pestaña de Gestion de Usuarios solo se muestra al Administrador.
 */
public class MainWindow extends JFrame {

    private final Usuario usuarioLogueado;
    private final GestionUsuarios gestionUsuarios;
    private final GestionLibreria gestionLibreria;
    private final GestionPrestamos gestionPrestamos;

    private JTabbedPane pestañas;

    public MainWindow(Usuario usuarioLogueado,
                      GestionUsuarios gestionUsuarios,
                      GestionLibreria gestionLibreria,
                      GestionPrestamos gestionPrestamos) {
        this.usuarioLogueado = usuarioLogueado;
        this.gestionUsuarios = gestionUsuarios;
        this.gestionLibreria = gestionLibreria;
        this.gestionPrestamos = gestionPrestamos;

        setTitle("Sistema de Gestion de Libreria - Usuario: " + usuarioLogueado.getUsername()
                + " (" + usuarioLogueado.getRol() + ")");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Barra de estado con el usuario actual
        JLabel lblEstado = new JLabel("Conectado como: " + usuarioLogueado.getUsername()
                + "  |  Rol: " + usuarioLogueado.getRol());
        add(lblEstado, BorderLayout.NORTH);

        pestañas = new JTabbedPane();

        // Pestaña Catalogo (libros) - ambos roles pueden consultar
        LibroPanel libroPanel = new LibroPanel(gestionLibreria, usuarioLogueado);
        pestañas.addTab("Catalogo de Libros", libroPanel);

        // Pestaña Prestamos - ambos roles
        PrestamoPanel prestamoPanel = new PrestamoPanel(gestionPrestamos, usuarioLogueado);
        pestañas.addTab("Prestamos y Devoluciones", prestamoPanel);

        // Pestaña Facturacion - ambos roles
        FacturaPanel facturaPanel = new FacturaPanel(gestionPrestamos, usuarioLogueado);
        pestañas.addTab("Facturacion de Multas", facturaPanel);

        // Pestaña Gestion de Usuarios - SOLO Administrador
        if (usuarioLogueado.getRol() == Rol.ADMINISTRADOR) {
            UsuarioPanel usuarioPanel = new UsuarioPanel(gestionUsuarios, usuarioLogueado);
            pestañas.addTab("Gestion de Usuarios", usuarioPanel);
        } else {
            // Opcional: mostrar una pestaña informativa bloqueada para cajero
            JLabel lblBloqueado = new JLabel(
                    "<html><center>Esta seccion es exclusiva del Administrador.<br>"
                            + "Su rol (Cajero) no tiene acceso a la gestion de perfiles.</center></html>",
                    SwingConstants.CENTER);
            pestañas.addTab("Gestion de Usuarios (Bloqueada)", lblBloqueado);
        }

        add(pestañas, BorderLayout.CENTER);

        // Boton cerrar sesion
        JButton btnCerrar = new JButton("Cerrar sesion");
        btnCerrar.addActionListener(e -> {
            int opcion = JOptionPane.showConfirmDialog(this,
                    "¿Desea cerrar la sesion?",
                    "Cerrar sesion",
                    JOptionPane.YES_NO_OPTION);
            if (opcion == JOptionPane.YES_OPTION) {
                dispose();
                LoginDialog login = new LoginDialog(gestionUsuarios);
                login.setLocationRelativeTo(null);
                login.setVisible(true);
                Usuario nuevoUsuario = login.getUsuarioAutenticado();
                if (nuevoUsuario != null) {
                    MainWindow nuevaVentana = new MainWindow(
                            nuevoUsuario, gestionUsuarios, gestionLibreria, gestionPrestamos);
                    nuevaVentana.setLocationRelativeTo(null);
                    nuevaVentana.setVisible(true);
                }
            }
        });

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSur.add(btnCerrar);
        add(panelSur, BorderLayout.SOUTH);
    }

}
