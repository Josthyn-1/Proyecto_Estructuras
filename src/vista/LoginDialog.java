package vista;

import seguridad.GestionUsuarios;
import seguridad.Usuario;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Ventana de inicio de sesión con autenticación contra GestionUsuarios.
 */
public class LoginDialog extends JDialog {

    private static final Color FONDO = new Color(24, 24, 27);
    private static final Color TEXTO_SECUNDARIO = new Color(161, 161, 170);

    private final GestionUsuarios gestionUsuarios;
    private Usuario usuarioAutenticado;

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginDialog(GestionUsuarios gestionUsuarios) {
        super((Frame) null, "Iniciar sesión - Jane Library", true);
        this.gestionUsuarios = gestionUsuarios;
        setSize(420, 480);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(FONDO);
        container.setBorder(BorderFactory.createEmptyBorder(28, 42, 30, 42));

        JLabel lblIcono = new JLabel("🔐", SwingConstants.CENTER);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("Bienvenido de nuevo");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Ingresa tus credenciales para acceder");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(TEXTO_SECUNDARIO);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUsuario = crearEtiqueta("Usuario");
        txtUsername = crearCampo();
        txtUsername.setToolTipText("Escribe tu nombre de usuario");

        JLabel lblPassword = crearEtiqueta("Contraseña");
        txtPassword = new JPasswordField();
        estilizarCampo(txtPassword);
        txtPassword.setToolTipText("Escribe tu contraseña");

        JButton btnIngresar = new JButton("Iniciar sesión");
        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnIngresar.setPreferredSize(new Dimension(320, 42));
        btnIngresar.setBackground(new Color(99, 102, 241));
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIngresar.setFocusPainted(false);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIngresar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIngresar.addActionListener(e -> autenticar());
        getRootPane().setDefaultButton(btnIngresar);

        container.add(lblIcono);
        container.add(Box.createRigidArea(new Dimension(0, 8)));
        container.add(lblTitulo);
        container.add(Box.createRigidArea(new Dimension(0, 4)));
        container.add(lblSubtitulo);
        container.add(Box.createRigidArea(new Dimension(0, 26)));
        container.add(lblUsuario);
        container.add(Box.createRigidArea(new Dimension(0, 6)));
        container.add(txtUsername);
        container.add(Box.createRigidArea(new Dimension(0, 16)));
        container.add(lblPassword);
        container.add(Box.createRigidArea(new Dimension(0, 6)));
        container.add(txtPassword);
        container.add(Box.createRigidArea(new Dimension(0, 24)));
        container.add(btnIngresar);

        setContentPane(container);
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(new Color(224, 224, 224));
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return etiqueta;
    }

    private JTextField crearCampo() {
        JTextField campo = new JTextField();
        estilizarCampo(campo);
        return campo;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        campo.setPreferredSize(new Dimension(320, 38));
        campo.setBackground(new Color(39, 39, 42));
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(Color.WHITE);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        Border borde = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(63, 63, 70)),
                BorderFactory.createEmptyBorder(5, 9, 5, 9));
        campo.setBorder(borde);
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

        if (gestionUsuarios.login(username, password)) {
            usuarioAutenticado = gestionUsuarios.getUsuarioAutenticado();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos.",
                    "Error de autenticación",
                    JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocusInWindow();
        }
    }

    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }
}
