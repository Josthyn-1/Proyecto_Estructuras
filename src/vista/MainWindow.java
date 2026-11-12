package vista;

import prestamos.GestionPrestamos;
import prestamos.Factura;
import prestamos.Prestamo;
import seguridad.GestionUsuarios;
import seguridad.Rol;
import seguridad.Usuario;
import servicios.GestionLibreria;
import modelo.Libro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ventana principal con panel lateral y dashboard en tema oscuro.
 */
public class MainWindow extends JFrame {

    private static final Color FONDO = new Color(9, 9, 11);
    private static final Color SUPERFICIE = new Color(24, 24, 27);
    private static final Color BORDE = new Color(39, 39, 42);
    private static final Color TEXTO = new Color(244, 244, 245);
    private static final Color TEXTO_SUAVE = new Color(161, 161, 170);
    private static final Color INDIGO = new Color(79, 70, 229);

    private final Usuario usuarioLogueado;
    private final GestionUsuarios gestionUsuarios;
    private final GestionLibreria gestionLibreria;
    private final GestionPrestamos gestionPrestamos;
    private final LibroPanel libroPanel;
    private final FacturaPanel facturaPanel;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel vistas = new JPanel(cardLayout);
    private final Map<String, JButton> botonesNavegacion = new HashMap<>();
    private final Map<String, JLabel> valoresMetricas = new HashMap<>();
    private final Map<String, String> titulosVistas = new HashMap<>();
    private JTextField txtBusqueda;
    private JLabel lblTituloVista;
    private JLabel lblSubtituloVista;

    public MainWindow(Usuario usuarioLogueado,
                      GestionUsuarios gestionUsuarios,
                      GestionLibreria gestionLibreria,
                      GestionPrestamos gestionPrestamos) {
        this.usuarioLogueado = usuarioLogueado;
        this.gestionUsuarios = gestionUsuarios;
        this.gestionLibreria = gestionLibreria;
        this.gestionPrestamos = gestionPrestamos;
        this.libroPanel = new LibroPanel(gestionLibreria, usuarioLogueado);
        this.facturaPanel = new FacturaPanel(gestionPrestamos, usuarioLogueado);

        setTitle("Sistema de Gestion de Libreria - Usuario: " + usuarioLogueado.getUsername()
                + " (" + usuarioLogueado.getRol() + ")");
        setSize(1280, 800);
        setMinimumSize(new Dimension(980, 650));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout());

        PrestamoPanel prestamoPanel = new PrestamoPanel(gestionPrestamos, usuarioLogueado);
        vistas.add(crearDashboard(), "dashboard");
        vistas.add(libroPanel, "libros");
        vistas.add(prestamoPanel, "prestamos");
        vistas.add(facturaPanel, "facturas");
        if (usuarioLogueado.getRol() == Rol.ADMINISTRADOR) {
            UsuarioPanel usuarioPanel = new UsuarioPanel(gestionUsuarios, usuarioLogueado);
            vistas.add(usuarioPanel, "usuarios");
        }

        add(crearBarraLateral(), BorderLayout.WEST);
        add(crearAreaPrincipal(), BorderLayout.CENTER);
        mostrarVista("dashboard");
        actualizarMetricas();
    }

    private JPanel crearBarraLateral() {
        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setPreferredSize(new Dimension(250, 0));
        lateral.setBackground(SUPERFICIE);
        lateral.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDE));

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setBorder(new EmptyBorder(20, 14, 14, 14));
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 11, 0));
        marca.setOpaque(false);
        JLabel icono = new JLabel("J", SwingConstants.CENTER);
        icono.setOpaque(true);
        icono.setBackground(INDIGO);
        icono.setForeground(Color.WHITE);
        icono.setFont(new Font("Segoe UI", Font.BOLD, 20));
        icono.setPreferredSize(new Dimension(42, 42));
        JLabel nombre = new JLabel("Jane Library");
        nombre.setForeground(TEXTO);
        nombre.setFont(new Font("Segoe UI", Font.BOLD, 17));
        marca.add(icono);
        marca.add(nombre);
        marca.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(marca);
        contenido.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel lblMenu = new JLabel("MENÚ PRINCIPAL");
        lblMenu.setForeground(TEXTO_SUAVE);
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblMenu.setBorder(new EmptyBorder(0, 10, 9, 0));
        lblMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(lblMenu);

        agregarBotonNavegacion(contenido, "dashboard", "Panel de control", "▦");
        agregarBotonNavegacion(contenido, "libros", "Libros", "▤");
        agregarBotonNavegacion(contenido, "prestamos", "Préstamos", "⇄");
        agregarBotonNavegacion(contenido, "facturas", "Facturación", "▧");
        if (usuarioLogueado.getRol() == Rol.ADMINISTRADOR) {
            agregarBotonNavegacion(contenido, "usuarios", "Usuarios", "♙");
        }

        lateral.add(contenido, BorderLayout.NORTH);
        lateral.add(crearPerfil(), BorderLayout.SOUTH);
        return lateral;
    }

    private void agregarBotonNavegacion(JPanel contenedor, String id, String texto, String icono) {
        JButton boton = new JButton(icono + "    " + texto);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        boton.setForeground(TEXTO_SUAVE);
        boton.setBackground(SUPERFICIE);
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(11, 12, 11, 10));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        boton.addActionListener(e -> mostrarVista(id));
        botonesNavegacion.put(id, boton);
        titulosVistas.put(id, texto);
        contenedor.add(boton);
        contenedor.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private JPanel crearPerfil() {
        JPanel perfil = new JPanel(new BorderLayout(10, 0));
        perfil.setBackground(SUPERFICIE);
        perfil.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE),
                new EmptyBorder(14, 16, 16, 12)));

        String iniciales = usuarioLogueado.getUsername().length() >= 2
                ? usuarioLogueado.getUsername().substring(0, 2).toUpperCase()
                : usuarioLogueado.getUsername().toUpperCase();
        JLabel avatar = new JLabel(iniciales, SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(49, 46, 129));
        avatar.setForeground(new Color(199, 210, 254));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        avatar.setPreferredSize(new Dimension(38, 38));

        JPanel datos = new JPanel();
        datos.setOpaque(false);
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
        JLabel usuario = new JLabel(usuarioLogueado.getUsername());
        usuario.setForeground(TEXTO);
        usuario.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel rol = new JLabel(usuarioLogueado.getRol().toString());
        rol.setForeground(TEXTO_SUAVE);
        rol.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        datos.add(usuario);
        datos.add(rol);

        JButton cerrar = new JButton("↪");
        cerrar.setToolTipText("Cerrar sesión");
        cerrar.setForeground(TEXTO_SUAVE);
        cerrar.setBackground(SUPERFICIE);
        cerrar.setFocusPainted(false);
        cerrar.setBorder(new EmptyBorder(5, 8, 5, 8));
        cerrar.addActionListener(e -> cerrarSesion());

        perfil.add(avatar, BorderLayout.WEST);
        perfil.add(datos, BorderLayout.CENTER);
        perfil.add(cerrar, BorderLayout.EAST);
        return perfil;
    }

    private JPanel crearAreaPrincipal() {
        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(FONDO);

        JPanel encabezado = new JPanel(new BorderLayout(20, 0));
        encabezado.setBackground(SUPERFICIE);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                new EmptyBorder(14, 28, 14, 28)));

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        lblTituloVista = new JLabel();
        lblTituloVista.setForeground(TEXTO);
        lblTituloVista.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSubtituloVista = new JLabel("Jane Library · Sistema de gestión");
        lblSubtituloVista.setForeground(TEXTO_SUAVE);
        lblSubtituloVista.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titulos.add(lblTituloVista);
        titulos.add(Box.createRigidArea(new Dimension(0, 3)));
        titulos.add(lblSubtituloVista);

        txtBusqueda = new JTextField(27);
        txtBusqueda.setToolTipText("Buscar libros por ID, título, autor, editorial o año");
        txtBusqueda.setPreferredSize(new Dimension(300, 36));
        txtBusqueda.addActionListener(e -> {
            libroPanel.buscar(txtBusqueda.getText());
            mostrarVista("libros");
        });
        JPanel buscador = new JPanel(new BorderLayout(8, 0));
        buscador.setOpaque(false);
        JLabel iconoBuscar = new JLabel("⌕");
        iconoBuscar.setForeground(TEXTO_SUAVE);
        iconoBuscar.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
        buscador.add(iconoBuscar, BorderLayout.WEST);
        buscador.add(txtBusqueda, BorderLayout.CENTER);

        encabezado.add(titulos, BorderLayout.WEST);
        encabezado.add(buscador, BorderLayout.EAST);
        principal.add(encabezado, BorderLayout.NORTH);

        vistas.setBackground(FONDO);
        principal.add(vistas, BorderLayout.CENTER);
        return principal;
    }

    private JPanel crearDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout(0, 24));
        dashboard.setBackground(FONDO);
        dashboard.setBorder(new EmptyBorder(30, 32, 30, 32));

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Panel de control");
        titulo.setForeground(TEXTO);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        JLabel descripcion = new JLabel("Resumen general de la biblioteca y sus actividades.");
        descripcion.setForeground(TEXTO_SUAVE);
        descripcion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        contenido.add(titulo);
        contenido.add(Box.createRigidArea(new Dimension(0, 5)));
        contenido.add(descripcion);
        contenido.add(Box.createRigidArea(new Dimension(0, 24)));

        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 14, 14));
        tarjetas.setOpaque(false);
        boolean esAdmin = usuarioLogueado.getRol() == Rol.ADMINISTRADOR;
        tarjetas.add(crearTarjetaMetrica("Libros registrados", "catalogo", "En el catálogo", new Color(129, 140, 248)));
        if (esAdmin) {
            tarjetas.add(crearTarjetaMetrica("Usuarios", "usuarios", "Cuentas del sistema", new Color(52, 211, 153)));
        } else {
            tarjetas.add(crearTarjetaMetrica("Facturas", "facturas", "Facturas vigentes", new Color(52, 211, 153)));
        }
        tarjetas.add(crearTarjetaMetrica("Préstamos activos", "prestamos", "Pendientes de devolución", new Color(251, 191, 36)));
        tarjetas.add(crearTarjetaMetrica("Recaudación", "recaudacion", "Facturas pagadas", new Color(34, 211, 238)));
        tarjetas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        tarjetas.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(tarjetas);
        contenido.add(Box.createRigidArea(new Dimension(0, 24)));

        JPanel accesoRapido = new JPanel(new BorderLayout());
        accesoRapido.setBackground(SUPERFICIE);
        accesoRapido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), new EmptyBorder(18, 20, 18, 20)));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel tituloAcceso = new JLabel("Accesos rápidos");
        tituloAcceso.setForeground(TEXTO);
        tituloAcceso.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel descripcionAcceso = new JLabel("Continúa con las tareas habituales del sistema.");
        descripcionAcceso.setForeground(TEXTO_SUAVE);
        descripcionAcceso.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textos.add(tituloAcceso);
        textos.add(Box.createRigidArea(new Dimension(0, 4)));
        textos.add(descripcionAcceso);

        JPanel accesos = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 12));
        accesos.setOpaque(false);
        accesos.add(crearBotonAcceso("Ver libros", "libros"));
        accesos.add(crearBotonAcceso("Registrar préstamo", "prestamos"));
        accesos.add(crearBotonAcceso("Ver facturación", "facturas"));
        accesoRapido.add(textos, BorderLayout.NORTH);
        accesoRapido.add(accesos, BorderLayout.CENTER);
        accesoRapido.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(accesoRapido);

        dashboard.add(contenido, BorderLayout.NORTH);
        return dashboard;
    }

    private JPanel crearTarjetaMetrica(String titulo, String clave, String descripcion, Color acento) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 15));
        tarjeta.setBackground(SUPERFICIE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), new EmptyBorder(16, 16, 16, 16)));

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setOpaque(false);
        JLabel lblTitulo = new JLabel(titulo.toUpperCase());
        lblTitulo.setForeground(TEXTO_SUAVE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        JLabel punto = new JLabel("●");
        punto.setForeground(acento);
        filaTitulo.add(lblTitulo, BorderLayout.WEST);
        filaTitulo.add(punto, BorderLayout.EAST);

        JLabel valor = new JLabel("—");
        valor.setForeground(TEXTO);
        valor.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valoresMetricas.put(clave, valor);
        JLabel lblDescripcion = new JLabel(descripcion);
        lblDescripcion.setForeground(TEXTO_SUAVE);
        lblDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JPanel detalle = new JPanel();
        detalle.setOpaque(false);
        detalle.setLayout(new BoxLayout(detalle, BoxLayout.Y_AXIS));
        detalle.add(valor);
        detalle.add(Box.createRigidArea(new Dimension(0, 3)));
        detalle.add(lblDescripcion);
        tarjeta.add(filaTitulo, BorderLayout.NORTH);
        tarjeta.add(detalle, BorderLayout.CENTER);
        return tarjeta;
    }

    private JButton crearBotonAcceso(String texto, String vista) {
        JButton boton = new JButton(texto);
        boton.setForeground(TEXTO);
        boton.setBackground(new Color(39, 39, 42));
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(9, 13, 9, 13));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addActionListener(e -> mostrarVista(vista));
        return boton;
    }

    private void mostrarVista(String id) {
        if ("usuarios".equals(id) && usuarioLogueado.getRol() != Rol.ADMINISTRADOR) {
            return;
        }
        cardLayout.show(vistas, id);
        lblTituloVista.setText("dashboard".equals(id) ? "Jane Library" : titulosVistas.get(id));
        lblSubtituloVista.setText("dashboard".equals(id)
                ? "Sistema de gestión · " + usuarioLogueado.getRol()
                : "Gestión de " + titulosVistas.get(id).toLowerCase());
        for (Map.Entry<String, JButton> entrada : botonesNavegacion.entrySet()) {
            boolean seleccionada = entrada.getKey().equals(id);
            entrada.getValue().setBackground(seleccionada ? INDIGO : SUPERFICIE);
            entrada.getValue().setForeground(seleccionada ? Color.WHITE : TEXTO_SUAVE);
        }
        if ("dashboard".equals(id)) {
            actualizarMetricas();
        } else if ("facturas".equals(id)) {
            facturaPanel.actualizarDatos();
        }
    }

    private void actualizarMetricas() {
        try {
            List<Libro> libros = gestionLibreria.consultarCatalogo(usuarioLogueado);
            List<Prestamo> prestamosActivos = gestionPrestamos.listarPrestamosActivos(usuarioLogueado);
            List<Factura> facturas = gestionPrestamos.listarFacturas(usuarioLogueado);
            double recaudacion = facturas.stream()
                    .filter(Factura::isPagada)
                    .mapToDouble(Factura::getMonto)
                    .sum();

            valoresMetricas.get("catalogo").setText(String.valueOf(libros.size()));
            valoresMetricas.get("prestamos").setText(String.valueOf(prestamosActivos.size()));
            JLabel facturasRegistradas = valoresMetricas.get("facturas");
            if (facturasRegistradas != null) {
                facturasRegistradas.setText(String.valueOf(facturas.size()));
            }
            valoresMetricas.get("recaudacion").setText(String.format("$%.2f", recaudacion));
            JLabel usuarios = valoresMetricas.get("usuarios");
            if (usuarios != null) {
                usuarios.setText(String.valueOf(gestionUsuarios.getUsuarios().size()));
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron cargar los datos del panel de control: " + ex.getMessage(),
                    "Error al actualizar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrarSesion() {
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Desea cerrar la sesión?",
                "Cerrar sesión",
                JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            gestionUsuarios.logout();
            dispose();
            LoginDialog login = new LoginDialog(gestionUsuarios);
            login.setVisible(true);
            Usuario nuevoUsuario = login.getUsuarioAutenticado();
            if (nuevoUsuario != null) {
                MainWindow nuevaVentana = new MainWindow(
                        nuevoUsuario, gestionUsuarios, gestionLibreria, gestionPrestamos);
                nuevaVentana.setLocationRelativeTo(null);
                nuevaVentana.setVisible(true);
            }
        }
    }

}
