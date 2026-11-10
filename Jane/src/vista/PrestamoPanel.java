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
    private JTextField txtIdLibro;
    private JTextField txtCedula;
    private JTextField txtNombres;
    private JTextField txtApellidos;
    private JTextField txtFechaPrestamo;
    private JTextField txtFechaEstimada;

    public PrestamoPanel(GestionPrestamos gestionPrestamos, Usuario usuarioLogueado) {
        this.gestionPrestamos = gestionPrestamos;
        this.usuarioLogueado = usuarioLogueado;
        setLayout(new BorderLayout(10, 10));
        initComponents();
        refrescarTabla();
    }

    private void initComponents() {
        JPanel panelForm = new JPanel(new GridLayout(2, 7, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Nuevo prestamo"));

        txtIdPrestamo = new JTextField();
        txtIdLibro = new JTextField();
        txtCedula = new JTextField();
        txtNombres = new JTextField();
        txtApellidos = new JTextField();
        txtFechaPrestamo = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        txtFechaEstimada = new JTextField(LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE));

        panelForm.add(new JLabel("ID prestamo:"));
        panelForm.add(txtIdPrestamo);
        panelForm.add(new JLabel("ID libro:"));
        panelForm.add(txtIdLibro);
        panelForm.add(new JLabel("Cedula (10 digitos):"));
        panelForm.add(txtCedula);
        panelForm.add(new JLabel("Nombres:"));
        panelForm.add(txtNombres);
        panelForm.add(new JLabel("Apellidos:"));
        panelForm.add(txtApellidos);
        panelForm.add(new JLabel("Fecha prestamo (YYYY-MM-DD):"));
        panelForm.add(txtFechaPrestamo);
        panelForm.add(new JLabel("Fecha estimada (YYYY-MM-DD):"));
        panelForm.add(txtFechaEstimada);

        JButton btnRegistrar = new JButton("Registrar prestamo");
        btnRegistrar.addActionListener(e -> registrarPrestamo());

        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.add(panelForm, BorderLayout.CENTER);
        panelNorte.add(btnRegistrar, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        String[] columnas = {"ID prestamo", "ID libro", "Libro", "Cedula", "Cliente", "Fecha prestamo", "Fecha estimada"};
        modeloActivos = new DefaultTableModel(columnas, 0);
        tablaActivos = new JTable(modeloActivos);

        String[] columnasHistorial = {"ID prestamo", "ID libro", "Libro", "Cedula", "Cliente",
            "Fecha prestamo", "Fecha estimada", "Fecha devolucion", "Estado"};
        modeloHistorial = new DefaultTableModel(columnasHistorial, 0);
        tablaHistorial = new JTable(modeloHistorial);

        JTabbedPane pestañasPrestamos = new JTabbedPane();
        pestañasPrestamos.addTab("Prestados ahora", new JScrollPane(tablaActivos));
        pestañasPrestamos.addTab("Historial", new JScrollPane(tablaHistorial));
        add(pestañasPrestamos, BorderLayout.CENTER);

        JButton btnDevolver = new JButton("Devolver prestamo seleccionado");
        btnDevolver.addActionListener(e -> devolverPrestamo());
        add(btnDevolver, BorderLayout.SOUTH);
    }

    private void registrarPrestamo() {
        try {
            String idPrestamo = txtIdPrestamo.getText().trim();
            int idLibro = Integer.parseInt(txtIdLibro.getText().trim());
                Cliente cliente = new Cliente(
                    txtCedula.getText(), txtNombres.getText(), txtApellidos.getText());
            LocalDate fechaPrestamo = LocalDate.parse(txtFechaPrestamo.getText().trim());
            LocalDate fechaEstimada = LocalDate.parse(txtFechaEstimada.getText().trim());

            Prestamo prestamo = gestionPrestamos.registrarPrestamo(
                    usuarioLogueado, idPrestamo, idLibro, cliente, fechaPrestamo, fechaEstimada);

            JOptionPane.showMessageDialog(this,
                    "Prestamo registrado: " + prestamo.getIdPrestamo(),
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            refrescarTabla();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "ID libro debe ser un numero valido.",
                    "Error de formato", JOptionPane.ERROR_MESSAGE);
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
        txtIdPrestamo.setText("");
        txtIdLibro.setText("");
        txtCedula.setText("");
        txtNombres.setText("");
        txtApellidos.setText("");
        txtFechaPrestamo.setText(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        txtFechaEstimada.setText(LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE));
        tablaActivos.clearSelection();
    }
}