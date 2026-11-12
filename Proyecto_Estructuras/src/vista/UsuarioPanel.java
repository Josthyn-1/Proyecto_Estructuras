package vista;

import excepciones.AccesoDenegadoException;
import excepciones.DatoInvalidoException;
import excepciones.UsuarioDuplicadoException;
import seguridad.GestionUsuarios;
import seguridad.Rol;
import seguridad.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de gestion de usuarios. Solo accesible por el Administrador.
 * Permite registrar y eliminar perfiles (Cajero o Administrador).
 */
public class UsuarioPanel extends JPanel {

    private final GestionUsuarios gestionUsuarios;
    private final Usuario usuarioLogueado;

    private DefaultTableModel tablaModelo;
    private JTable tablaUsuarios;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<Rol> cmbRol;

    public UsuarioPanel(GestionUsuarios gestionUsuarios, Usuario usuarioLogueado) {
        this.gestionUsuarios = gestionUsuarios;
        this.usuarioLogueado = usuarioLogueado;
        setLayout(new BorderLayout(10, 10));
        initComponents();
        refrescarTabla();
    }


    private void initComponents() {
        // Formulario de registro
        JPanel panelForm = new JPanel(new GridLayout(4, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Registro de nuevo usuario"));

        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        cmbRol = new JComboBox<>(Rol.values());

        panelForm.add(new JLabel("Nombre de usuario:"));
        panelForm.add(txtUsername);
        panelForm.add(new JLabel("Contraseña:"));
        panelForm.add(txtPassword);
        panelForm.add(new JLabel("Rol:"));
        panelForm.add(cmbRol);

        JButton btnRegistrar = new JButton("Registrar usuario");
        btnRegistrar.addActionListener(e -> registrarUsuario());

        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.add(panelForm, BorderLayout.CENTER);
        panelNorte.add(btnRegistrar, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        // Tabla de usuarios
        String[] columnas = {"Usuario", "Rol"};
        tablaModelo = new DefaultTableModel(columnas, 0);
        tablaUsuarios = new JTable(tablaModelo);
        add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);

        JButton btnEliminar = new JButton("Eliminar usuario seleccionado");
        btnEliminar.addActionListener(e -> eliminarUsuario());
        add(btnEliminar, BorderLayout.SOUTH);
    }

    private void registrarUsuario() {
        try {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            Rol rol = (Rol) cmbRol.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                throw new DatoInvalidoException("Usuario y contraseña son obligatorios.");
            }

            Usuario nuevo = new Usuario(username, password, rol);
            gestionUsuarios.registrarUsuario(usuarioLogueado, nuevo);

            JOptionPane.showMessageDialog(this,
                    "Usuario '" + username + "' registrado correctamente.",
                    "Exito",
                    JOptionPane.INFORMATION_MESSAGE);

            txtUsername.setText("");
            txtPassword.setText("");
            cmbRol.setSelectedIndex(0);
            refrescarTabla();
        } catch (AccesoDenegadoException | UsuarioDuplicadoException | DatoInvalidoException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refrescarTabla() {
        tablaModelo.setRowCount(0);
        List<Usuario> usuarios = gestionUsuarios.getUsuarios();
        for (Usuario u : usuarios) {
            tablaModelo.addRow(new Object[]{u.getUsername(), u.getRol()});
        }
    }

    private void eliminarUsuario() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione el usuario que desea eliminar.",
                    "Usuario no seleccionado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaUsuarios.convertRowIndexToModel(filaSeleccionada);
        String username = (String) tablaModelo.getValueAt(filaModelo, 0);
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar al usuario '" + username + "'?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            gestionUsuarios.eliminarUsuario(usuarioLogueado, username);
            refrescarTabla();
            JOptionPane.showMessageDialog(this,
                    "El usuario '" + username + "' fue eliminado.",
                    "Usuario eliminado",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (AccesoDenegadoException | DatoInvalidoException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}