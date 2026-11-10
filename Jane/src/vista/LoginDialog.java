package vista;

import excepciones.UsuarioDuplicadoException;
import seguridad.GestionUsuarios;
import seguridad.Rol;
import seguridad.Usuario;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana de inicio de sesion.
 * Autentica al usuario contra GestionUsuarios y expone el resultado
 * mediante getUsuarioAutenticado().
 */
public class LoginDialog extends JDialog {

    private final GestionUsuarios gestionUsuarios;
    private Usuario usuarioAutenticado;

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginDialog(GestionUsuarios gestionUsuarios) {
        this.gestionUsuarios = gestionUsuarios;
        setTitle("Libreria - Inicio de sesion");
        setModal(true);
        setSize(400, 250);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblTitulo = new JLabel("Sistema de Gestion de Libreria");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(lblTitulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        add(new JLabel("Usuario:"), gbc);
        txtUsername = new JTextField(20);
        gbc.gridx = 1;
        add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Contraseña:"), gbc);
        txtPassword = new JPasswordField(20);
        gbc.gridx = 1;
        add(txtPassword, gbc);

        JButton btnLogin = new JButton("Iniciar sesion");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnLogin, gbc);

        btnLogin.addActionListener(e -> autenticar());
    }

    private void autenticar() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese usuario y contraseña.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = gestionUsuarios.login(username, password);
        if (ok) {
            usuarioAutenticado = gestionUsuarios.getUsuarioAutenticado();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos.",
                    "Error de autenticacion",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }
}
