package vista;

import excepciones.AccesoDenegadoException;
import excepciones.DatoInvalidoException;
import prestamos.Factura;
import prestamos.GestionPrestamos;
import seguridad.Rol;
import seguridad.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de facturacion de multas. Accesible por Cajero y Administrador.
 */
public class FacturaPanel extends JPanel {

    private final GestionPrestamos gestionPrestamos;
    private final Usuario usuarioLogueado;

    private DefaultTableModel modeloFacturas;
    private JTable tablaFacturas;
    private DefaultTableModel modeloHistorial;
    private JTable tablaHistorial;
    private JTextField txtIdPrestamo;

    public FacturaPanel(GestionPrestamos gestionPrestamos, Usuario usuarioLogueado) {
        this.gestionPrestamos = gestionPrestamos;
        this.usuarioLogueado = usuarioLogueado;
        setLayout(new BorderLayout(10, 10));
        initComponents();
        refrescarTabla();
    }

    private void initComponents() {
        // Formulario de factura
        JPanel panelForm = new JPanel(new GridLayout(1, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Generar factura de multa"));

        txtIdPrestamo = new JTextField();

        panelForm.add(new JLabel("ID prestamo:"));
        panelForm.add(txtIdPrestamo);

        JButton btnGenerar = new JButton("Generar factura");
        btnGenerar.addActionListener(e -> generarFactura());

        JButton btnModificar = new JButton("Modificar factura");
        btnModificar.addActionListener(e -> modificarFactura());

        JButton btnEliminar = new JButton("Eliminar factura");
        btnEliminar.addActionListener(e -> eliminarFactura());

        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.add(panelForm, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnGenerar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        boolean puedeGestionar = usuarioLogueado != null
                && (usuarioLogueado.getRol() == Rol.ADMINISTRADOR
                || usuarioLogueado.getRol() == Rol.CAJERO);
        txtIdPrestamo.setEnabled(puedeGestionar);
        btnGenerar.setEnabled(puedeGestionar);
        btnModificar.setEnabled(puedeGestionar);
        btnEliminar.setEnabled(puedeGestionar);
        panelNorte.add(panelBotones, BorderLayout.SOUTH);

        add(panelNorte, BorderLayout.NORTH);

        // Tabla de facturas
        String[] columnas = {"ID factura", "ID prestamo", "Cliente", "Monto", "Pagada"};
        modeloFacturas = crearModelo(columnas);
        tablaFacturas = new JTable(modeloFacturas);

        String[] columnasHistorial = {"ID factura", "ID prestamo", "Cliente", "Monto", "Pagada",
                "Estado", "Emision", "Eliminacion"};
        modeloHistorial = crearModelo(columnasHistorial);
        tablaHistorial = new JTable(modeloHistorial);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Facturas vigentes", new JScrollPane(tablaFacturas));
        pestanas.addTab("Historial", new JScrollPane(tablaHistorial));
        add(pestanas, BorderLayout.CENTER);
    }

    private DefaultTableModel crearModelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private void generarFactura() {
        try {
            String idPrestamo = txtIdPrestamo.getText().trim();
            Factura factura = gestionPrestamos.generarFactura(usuarioLogueado, idPrestamo);

            refrescarTabla();

            JOptionPane.showMessageDialog(this,
                    "Factura generada: " + factura.getIdFactura()
                            + "\nMonto: $" + String.format("%.2f", factura.getMonto()),
                    "Exito", JOptionPane.INFORMATION_MESSAGE);

            txtIdPrestamo.setText("");
        } catch (AccesoDenegadoException | DatoInvalidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            refrescarTabla();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese un monto numerico valido.", "Error de formato", JOptionPane.ERROR_MESSAGE);
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
                    "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }

            gestionPrestamos.eliminarFactura(usuarioLogueado, factura.getIdFactura());
            refrescarTabla();
        } catch (AccesoDenegadoException | DatoInvalidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refrescarTabla() {
        modeloFacturas.setRowCount(0);
        for (Factura factura : gestionPrestamos.listarFacturas(usuarioLogueado)) {
            modeloFacturas.addRow(new Object[]{
                    factura.getIdFactura(),
                    factura.getIdPrestamo(),
                    factura.getNombreCliente(),
                    String.format("%.2f", factura.getMonto()),
                    factura.isPagada() ? "Si" : "No"
            });
        }

        modeloHistorial.setRowCount(0);
        List<Factura> historial = gestionPrestamos.listarHistorialFacturas(usuarioLogueado);
        for (Factura factura : historial) {
            modeloHistorial.addRow(new Object[]{
                    factura.getIdFactura(),
                    factura.getIdPrestamo(),
                    factura.getNombreCliente(),
                    String.format("%.2f", factura.getMonto()),
                    factura.isPagada() ? "Si" : "No",
                    factura.isEliminada() ? "Eliminada" : "Vigente",
                    factura.getFechaEmision(),
                    factura.getFechaEliminacion() == null ? "-" : factura.getFechaEliminacion()
            });
        }
    }
}
