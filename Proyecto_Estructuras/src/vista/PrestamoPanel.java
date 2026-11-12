package vista;

import excepciones.AccesoDenegadoException;
import excepciones.DatoInvalidoException;
import excepciones.LibroNoEncontradoException;
import modelo.Cliente;
import modelo.Libro;
import prestamos.GestionPrestamos;
import prestamos.Prestamo;
import seguridad.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Panel de prestamos y devoluciones. Accesible por Cajero y Administrador.
 */
public class PrestamoPanel extends JPanel {

    private final GestionPrestamos gestionPrestamos;
    private final Usuario usuarioLogueado;

    private DefaultTableModel modeloActivos;
    private JTable tablaActivos;
    private DefaultTableModel modeloHistorial;
    private JTable tablaHistorial;

    private JTextField txtIdPrestamo;
    private JComboBox<Libro> cmbLibrosDisponibles;
    private JTextField txtCedula;
    private JTextField txtNombres;
    private JTextField txtApellidos;
    private JTextField txtFechaPrestamo;
    private JTextField txtFechaEstimada;

    public PrestamoPanel(GestionPrestamos gestionPrestamos, Usuario usuarioLogueado) {
        this.gestionPrestamos = gestionPrestamos;
        this.usuarioLogueado = usuarioLogueado;
        setBackground(new Color(24, 24, 27));
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        setLayout(new BorderLayout(14, 14));
        initComponents();
        cargarLibrosDisponibles();
        refrescarTabla();
    }

    private void initComponents() {
        JPanel panelSuperior = new JPanel(new BorderLayout(0, 12));
        panelSuperior.setOpaque(false);

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Préstamos y devoluciones");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel subtitulo = new JLabel("Registra un préstamo seleccionando un libro disponible.");
        subtitulo.setForeground(new Color(161, 161, 170));
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        encabezado.add(titulo);
        encabezado.add(Box.createRigidArea(new Dimension(0, 4)));
        encabezado.add(subtitulo);
        panelSuperior.add(encabezado, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new BorderLayout(0, 12));
        panelForm.setBackground(new Color(24, 24, 27));
        panelForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(39, 39, 42)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel tituloForm = new JPanel(new BorderLayout());
        tituloForm.setOpaque(false);
        JLabel lblNuevoPrestamo = new JLabel("Nuevo préstamo");
        lblNuevoPrestamo.setForeground(Color.WHITE);
        lblNuevoPrestamo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel lblRequeridos = new JLabel("Completa los datos del cliente y del préstamo");
        lblRequeridos.setForeground(new Color(161, 161, 170));
        lblRequeridos.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tituloForm.add(lblNuevoPrestamo, BorderLayout.NORTH);
        tituloForm.add(lblRequeridos, BorderLayout.SOUTH);
        panelForm.add(tituloForm, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(0, 4, 12, 10));
        campos.setOpaque(false);
        txtIdPrestamo = new JTextField(generarIdPrestamo());
        txtIdPrestamo.setToolTipText("Identificador único generado; puedes modificarlo si lo necesitas.");
        cmbLibrosDisponibles = new JComboBox<>();
        cmbLibrosDisponibles.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Libro) {
                    Libro libro = (Libro) value;
                    setText(libro.getId() + " — " + libro.getNombreLibro());
                } else {
                    setText("Selecciona un libro disponible");
                }
                return this;
            }
        });
        txtCedula = new JTextField();
        txtNombres = new JTextField();
        txtApellidos = new JTextField();
        txtFechaPrestamo = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        txtFechaEstimada = new JTextField(LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE));
        txtCedula.setToolTipText("Debe contener exactamente 10 dígitos.");
        txtFechaPrestamo.setToolTipText("Formato: AAAA-MM-DD");
        txtFechaEstimada.setToolTipText("Formato: AAAA-MM-DD");

        campos.add(crearCampo("ID del préstamo", txtIdPrestamo));
        campos.add(crearCampo("Libro disponible", cmbLibrosDisponibles));
        campos.add(crearCampo("Cédula (10 dígitos)", txtCedula));
        campos.add(crearCampo("Nombres", txtNombres));
        campos.add(crearCampo("Apellidos", txtApellidos));
        campos.add(crearCampo("Fecha del préstamo (AAAA-MM-DD)", txtFechaPrestamo));
        campos.add(crearCampo("Devolución estimada (AAAA-MM-DD)", txtFechaEstimada));
        panelForm.add(campos, BorderLayout.CENTER);

        JPanel panelAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelAccion.setOpaque(false);
        JButton btnRegistrar = new JButton("+ Registrar préstamo");
        btnRegistrar.setBackground(new Color(79, 70, 229));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegistrar.setPreferredSize(new Dimension(190, 40));
        btnRegistrar.addActionListener(e -> registrarPrestamo());
        panelAccion.add(btnRegistrar);
        panelForm.add(panelAccion, BorderLayout.SOUTH);
        panelSuperior.add(panelForm, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);

        String[] columnas = {"ID prestamo", "ID libro", "Libro", "Cedula", "Cliente", "Fecha prestamo", "Fecha estimada"};
        modeloActivos = new DefaultTableModel(columnas, 0);
        tablaActivos = new JTable(modeloActivos);
        estilizarTabla(tablaActivos);

        String[] columnasHistorial = {"ID prestamo", "ID libro", "Libro", "Cedula", "Cliente",
            "Fecha prestamo", "Fecha estimada", "Fecha devolucion", "Estado"};
        modeloHistorial = new DefaultTableModel(columnasHistorial, 0);
        tablaHistorial = new JTable(modeloHistorial);
        estilizarTabla(tablaHistorial);

        JTabbedPane pestañasPrestamos = new JTabbedPane();
        pestañasPrestamos.addTab("Prestados ahora", new JScrollPane(tablaActivos));
        pestañasPrestamos.addTab("Historial", new JScrollPane(tablaHistorial));
        add(pestañasPrestamos, BorderLayout.CENTER);

        JButton btnDevolver = new JButton("Registrar devolución del préstamo seleccionado");
        btnDevolver.setBackground(new Color(63, 63, 70));
        btnDevolver.setForeground(Color.WHITE);
        btnDevolver.setFocusPainted(false);
        btnDevolver.addActionListener(e -> devolverPrestamo());
        JPanel panelDevolucion = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelDevolucion.setOpaque(false);
        panelDevolucion.add(btnDevolver);
        add(panelDevolucion, BorderLayout.SOUTH);
    }

    private JPanel crearCampo(String titulo, JComponent campo) {
        JPanel grupo = new JPanel();
        grupo.setOpaque(false);
        grupo.setLayout(new BoxLayout(grupo, BoxLayout.Y_AXIS));
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setForeground(new Color(212, 212, 216));
        etiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        grupo.add(etiqueta);
        grupo.add(Box.createRigidArea(new Dimension(0, 5)));
        grupo.add(campo);
        return grupo;
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(30);
        tabla.setFillsViewportHeight(true);
        tabla.setShowVerticalLines(false);
        tabla.setSelectionBackground(new Color(67, 56, 202));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    private String generarIdPrestamo() {
        return "PRE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void cargarLibrosDisponibles() {
        cmbLibrosDisponibles.removeAllItems();
        cmbLibrosDisponibles.addItem(null);
        try {
            for (Libro libro : gestionPrestamos.listarLibrosDisponibles(usuarioLogueado)) {
                cmbLibrosDisponibles.addItem(libro);
            }
        } catch (AccesoDenegadoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acceso denegado", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarPrestamo() {
        try {
            String idPrestamo = txtIdPrestamo.getText().trim();
            Libro libroSeleccionado = (Libro) cmbLibrosDisponibles.getSelectedItem();
            if (libroSeleccionado == null) {
                JOptionPane.showMessageDialog(this,
                        "Selecciona un libro disponible antes de registrar el préstamo.",
                        "Falta seleccionar el libro", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Cliente cliente = new Cliente(
                    txtCedula.getText(), txtNombres.getText(), txtApellidos.getText());
            LocalDate fechaPrestamo = LocalDate.parse(txtFechaPrestamo.getText().trim());
            LocalDate fechaEstimada = LocalDate.parse(txtFechaEstimada.getText().trim());

            Prestamo prestamo = gestionPrestamos.registrarPrestamo(
                    usuarioLogueado, idPrestamo, libroSeleccionado.getId(), cliente, fechaPrestamo, fechaEstimada);

            JOptionPane.showMessageDialog(this,
                    "Prestamo registrado: " + prestamo.getIdPrestamo(),
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            refrescarTabla();
            cargarLibrosDisponibles();
        } catch (DateTimeException e) {
            JOptionPane.showMessageDialog(this,
                    "Fecha invalida. Use el formato YYYY-MM-DD.",
                    "Error de fecha", JOptionPane.ERROR_MESSAGE);
        } catch (AccesoDenegadoException | DatoInvalidoException | LibroNoEncontradoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void devolverPrestamo() {
        try {
            int fila = tablaActivos.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(this,
                        "Seleccione un prestamo de la pestaña Prestados ahora.",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String idPrestamo = modeloActivos.getValueAt(fila, 0).toString();
            double multa = gestionPrestamos.registrarDevolucion(usuarioLogueado, idPrestamo);

            String msg = "Prestamo devuelto correctamente.";
            if (multa > 0) {
                msg += "\nMulta por retraso: $" + String.format("%.2f", multa);
            }

            JOptionPane.showMessageDialog(this, msg, "Devolucion", JOptionPane.INFORMATION_MESSAGE);
            refrescarTabla();
            cargarLibrosDisponibles();
        } catch (AccesoDenegadoException | DatoInvalidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refrescarTabla() {
        modeloActivos.setRowCount(0);
        modeloHistorial.setRowCount(0);
        try {
            List<Prestamo> prestamos = gestionPrestamos.listarPrestamosActivos(usuarioLogueado);
            for (Prestamo p : prestamos) {
                Libro libro = p.getLibroPrestado();
                modeloActivos.addRow(datosPrestamo(p, libro));
            }

            for (Prestamo p : gestionPrestamos.listarHistorialPrestamos(usuarioLogueado)) {
                Libro libro = p.getLibroPrestado();
                modeloHistorial.addRow(new Object[]{
                        p.getIdPrestamo(),
                        libro.getId(),
                        libro.getNombreLibro(),
                        p.getCliente().getCedula(),
                        p.getCliente().getNombreCompleto(),
                        p.getFechaPrestamo(),
                        p.getFechaDevolucionEstimada(),
                        p.getFechaDevolucionReal() == null ? "-" : p.getFechaDevolucionReal(),
                        p.isActivo() ? "Prestado" : "Devuelto"
                });
            }
        } catch (AccesoDenegadoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acceso denegado", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Object[] datosPrestamo(Prestamo prestamo, Libro libro) {
        return new Object[]{
                prestamo.getIdPrestamo(),
                libro.getId(),
                libro.getNombreLibro(),
                prestamo.getCliente().getCedula(),
                prestamo.getCliente().getNombreCompleto(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaDevolucionEstimada()
        };
    }

    private void limpiarCampos() {
        txtIdPrestamo.setText(generarIdPrestamo());
        cmbLibrosDisponibles.setSelectedIndex(0);
        txtCedula.setText("");
        txtNombres.setText("");
        txtApellidos.setText("");
        txtFechaPrestamo.setText(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        txtFechaEstimada.setText(LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE));
        tablaActivos.clearSelection();
    }
}