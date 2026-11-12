package vista;

import excepciones.AccesoDenegadoException;
import excepciones.DatoInvalidoException;
import prestamos.Factura;
import prestamos.GestionPrestamos;
import prestamos.Prestamo;
import seguridad.Rol;
import seguridad.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Panel de facturación de multas. Solo se factura una mora después de devolver
 * el préstamo y cuando la devolución fue posterior a la fecha estimada.
 */
public class FacturaPanel extends JPanel {

    private final GestionPrestamos gestionPrestamos;
    private final Usuario usuarioLogueado;

    private DefaultTableModel modeloFacturas;
    private JTable tablaFacturas;
    private DefaultTableModel modeloHistorial;
    private JTable tablaHistorial;
    private JComboBox<Prestamo> cmbPrestamosPendientes;
    private JLabel lblDetalleMora;

    public FacturaPanel(GestionPrestamos gestionPrestamos, Usuario usuarioLogueado) {
        this.gestionPrestamos = gestionPrestamos;
        this.usuarioLogueado = usuarioLogueado;
        setBackground(new Color(24, 24, 27));
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        setLayout(new BorderLayout(10, 14));
        initComponents();
        actualizarDatos();
    }

    private void initComponents() {
        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Facturación de multas");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel descripcion = new JLabel(
                "Solo se genera factura si el préstamo fue devuelto después de la fecha estimada.");
        descripcion.setForeground(new Color(161, 161, 170));
        descripcion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        encabezado.add(titulo);
        encabezado.add(Box.createRigidArea(new Dimension(0, 4)));
        encabezado.add(descripcion);

        JPanel panelForm = new JPanel(new BorderLayout(10, 10));
        panelForm.setBackground(new Color(24, 24, 27));
        panelForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(39, 39, 42)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel seleccion = new JPanel(new GridLayout(0, 1, 5, 5));
        seleccion.setOpaque(false);
        JLabel lblSeleccion = new JLabel("Préstamos devueltos con mora pendiente");
        lblSeleccion.setForeground(new Color(212, 212, 216));
        cmbPrestamosPendientes = new JComboBox<>();
        cmbPrestamosPendientes.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Prestamo) {
                    Prestamo prestamo = (Prestamo) value;
                    setText(prestamo.getIdPrestamo() + " — "
                            + prestamo.getCliente().getNombreCompleto() + " — "
                            + prestamo.getLibroPrestado().getNombreLibro());
                } else {
                    setText("No hay préstamos pendientes de facturar");
                }
                return this;
            }
        });
        lblDetalleMora = new JLabel("Mora: —");
        lblDetalleMora.setForeground(new Color(251, 191, 36));
        lblDetalleMora.setFont(new Font("Segoe UI", Font.BOLD, 12));
        seleccion.add(lblSeleccion);
        seleccion.add(cmbPrestamosPendientes);
        seleccion.add(lblDetalleMora);
        panelForm.add(seleccion, BorderLayout.CENTER);

        JButton btnGenerar = new JButton("Generar factura");
        btnGenerar.setBackground(new Color(79, 70, 229));
        btnGenerar.setForeground(Color.WHITE);
        btnGenerar.setFocusPainted(false);
        btnGenerar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGenerar.addActionListener(e -> generarFactura());

        JButton btnModificar = new JButton("Modificar factura");
        btnModificar.addActionListener(e -> modificarFactura());
        JButton btnEliminar = new JButton("Eliminar factura");
        btnEliminar.addActionListener(e -> eliminarFactura());
        JButton btnActualizar = new JButton("Actualizar lista");
        btnActualizar.addActionListener(e -> actualizarDatos());

        boolean puedeGestionar = usuarioLogueado != null
                && (usuarioLogueado.getRol() == Rol.ADMINISTRADOR
                || usuarioLogueado.getRol() == Rol.CAJERO);
        cmbPrestamosPendientes.setEnabled(puedeGestionar);
        btnGenerar.setEnabled(puedeGestionar);
        btnModificar.setEnabled(puedeGestionar);
        btnEliminar.setEnabled(puedeGestionar);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnGenerar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);

        JPanel panelNorte = new JPanel(new BorderLayout(0, 12));
        panelNorte.setOpaque(false);
        panelNorte.add(encabezado, BorderLayout.NORTH);
        panelNorte.add(panelForm, BorderLayout.CENTER);
        panelNorte.add(panelBotones, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        String[] columnas = {"ID factura", "ID préstamo", "Cliente", "Monto", "Pagada"};
        modeloFacturas = crearModelo(columnas);
        tablaFacturas = new JTable(modeloFacturas);
        estilizarTabla(tablaFacturas);

        String[] columnasHistorial = {"ID factura", "ID préstamo", "Cliente", "Monto", "Pagada",
                "Estado", "Emisión", "Eliminación"};
        modeloHistorial = crearModelo(columnasHistorial);
        tablaHistorial = new JTable(modeloHistorial);
        estilizarTabla(tablaHistorial);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Facturas vigentes", new JScrollPane(tablaFacturas));
        pestanas.addTab("Historial", new JScrollPane(tablaHistorial));
        add(pestanas, BorderLayout.CENTER);

        cmbPrestamosPendientes.addActionListener(e -> actualizarDetalleMora());
    }

    private DefaultTableModel crearModelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(30);
        tabla.setFillsViewportHeight(true);
        tabla.setShowVerticalLines(false);
        tabla.setSelectionBackground(new Color(67, 56, 202));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    private void generarFactura() {
        Prestamo prestamoSeleccionado = (Prestamo) cmbPrestamosPendientes.getSelectedItem();
        if (prestamoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay préstamos devueltos con mora pendiente. La mora solo se cobra "
                            + "si la devolución fue posterior a la fecha estimada.",
                    "Sin mora pendiente", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Factura factura = gestionPrestamos.generarFactura(
                    usuarioLogueado, prestamoSeleccionado.getIdPrestamo());
            actualizarDatos();
            JOptionPane.showMessageDialog(this,
                    "Factura generada: " + factura.getIdFactura()
                            + "\nCliente: " + factura.getNombreCliente()
                            + "\nMonto de la mora: $" + String.format("%.2f", factura.getMonto()),
                    "Factura generada", JOptionPane.INFORMATION_MESSAGE);
        } catch (AccesoDenegadoException | DatoInvalidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            actualizarDatos();
        }
    }

    private void modificarFactura() {
        int fila = tablaFacturas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una factura vigente de la tabla.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Factura factura = gestionPrestamos.listarFacturas(usuarioLogueado).get(fila);
        JTextField txtMonto = new JTextField(String.format("%.2f", factura.getMonto()));
        JCheckBox chkPagada = new JCheckBox("Pagada", factura.isPagada());
        JPanel formulario = new JPanel(new GridLayout(2, 2, 6, 6));
        formulario.add(new JLabel("Monto:"));
        formulario.add(txtMonto);
        formulario.add(new JLabel("Estado:"));
        formulario.add(chkPagada);

        int opcion = JOptionPane.showConfirmDialog(this, formulario,
                "Modificar factura " + factura.getIdFactura(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            double monto = Double.parseDouble(txtMonto.getText().trim().replace(',', '.'));
            gestionPrestamos.modificarFactura(
                    usuarioLogueado, factura.getIdFactura(), monto, chkPagada.isSelected());
            actualizarDatos();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese un monto numérico válido.", "Error de formato", JOptionPane.ERROR_MESSAGE);
        } catch (AccesoDenegadoException | DatoInvalidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarFactura() {
        int fila = tablaFacturas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una factura vigente de la tabla.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Factura factura = gestionPrestamos.listarFacturas(usuarioLogueado).get(fila);
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Desea eliminar la factura " + factura.getIdFactura() + "?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }

            gestionPrestamos.eliminarFactura(usuarioLogueado, factura.getIdFactura());
            actualizarDatos();
        } catch (AccesoDenegadoException | DatoInvalidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refrescarTabla() {
        modeloFacturas.setRowCount(0);
        for (Factura factura : gestionPrestamos.listarFacturas(usuarioLogueado)) {
            modeloFacturas.addRow(new Object[]{
                    factura.getIdFactura(), factura.getIdPrestamo(), factura.getNombreCliente(),
                    String.format("%.2f", factura.getMonto()), factura.isPagada() ? "Sí" : "No"
            });
        }

        modeloHistorial.setRowCount(0);
        List<Factura> historial = gestionPrestamos.listarHistorialFacturas(usuarioLogueado);
        for (Factura factura : historial) {
            modeloHistorial.addRow(new Object[]{
                    factura.getIdFactura(), factura.getIdPrestamo(), factura.getNombreCliente(),
                    String.format("%.2f", factura.getMonto()), factura.isPagada() ? "Sí" : "No",
                    factura.isEliminada() ? "Eliminada" : "Vigente", factura.getFechaEmision(),
                    factura.getFechaEliminacion() == null ? "-" : factura.getFechaEliminacion()
            });
        }
    }

    public void actualizarDatos() {
        refrescarTabla();
        cargarPrestamosPendientes();
    }

    private void cargarPrestamosPendientes() {
        cmbPrestamosPendientes.removeAllItems();
        try {
            List<Prestamo> pendientes = gestionPrestamos.listarPrestamosConMoraPendiente(usuarioLogueado);
            for (Prestamo prestamo : pendientes) {
                cmbPrestamosPendientes.addItem(prestamo);
            }
            cmbPrestamosPendientes.setEnabled(!pendientes.isEmpty());
            if (pendientes.isEmpty()) {
                lblDetalleMora.setText("No hay mora pendiente. Se cobran $200 por cada día de retraso.");
            } else {
                actualizarDetalleMora();
            }
        } catch (AccesoDenegadoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acceso denegado", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarDetalleMora() {
        Prestamo prestamo = (Prestamo) cmbPrestamosPendientes.getSelectedItem();
        if (prestamo == null) {
            return;
        }
        long diasRetraso = ChronoUnit.DAYS.between(
                prestamo.getFechaDevolucionEstimada(), prestamo.getFechaDevolucionReal());
        double monto = gestionPrestamos.calcularMulta(prestamo);
        lblDetalleMora.setText("Devuelto " + diasRetraso + " día(s) tarde · Total de mora: $"
                + String.format("%.2f", monto));
    }
}
