package vista;

import excepciones.AccesoDenegadoException;
import excepciones.DatoInvalidoException;
import excepciones.LibroDuplicadoException;
import excepciones.LibroNoEncontradoException;
import modelo.Libro;
import seguridad.Rol;
import seguridad.Usuario;
import servicios.GestionLibreria;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Panel de CRUD de libros.
 * ADMINISTRADOR: acceso total (agregar, actualizar, eliminar).
 * CAJERO: solo lectura (consultar catalogo).
 */
public class LibroPanel extends JPanel {

    private final GestionLibreria gestionLibreria;
    private final Usuario usuarioLogueado;

    private DefaultTableModel tablaModelo;
    private JTable tablaLibros;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtBuscar;
    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtAutor;
    private JTextField txtEdicion;
    private JTextField txtEditorial;
    private JTextField txtAnio;

    public LibroPanel(GestionLibreria gestionLibreria, Usuario usuarioLogueado) {
        this.gestionLibreria = gestionLibreria;
        this.usuarioLogueado = usuarioLogueado;
        setLayout(new BorderLayout(10, 10));
        initComponents();
        refrescarTabla();
    }

    private void initComponents() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout(12, 12));
        headerPanel.setOpaque(false);

        JLabel lblTitulo = new JLabel("Gestión de Libros");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelAcciones.setOpaque(false);
        txtBuscar = new JTextField(20);
        txtBuscar.setToolTipText("Buscar por ID, título, autor, editorial o año");
        txtBuscar.setPreferredSize(new Dimension(240, 36));

        JButton btnAgregar = new JButton("+ Agregar libro");
        btnAgregar.setBackground(new Color(99, 102, 241));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAgregar.addActionListener(e -> agregarLibro());

        panelAcciones.add(txtBuscar);
        panelAcciones.add(btnAgregar);
        headerPanel.add(lblTitulo, BorderLayout.WEST);
        headerPanel.add(panelAcciones, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        String[] columnas = {"ID", "Título", "Autor", "Edición", "Editorial", "Año"};
        tablaModelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaLibros = new JTable(tablaModelo);
        tablaLibros.setRowHeight(34);
        tablaLibros.setFillsViewportHeight(true);
        tablaLibros.setShowVerticalLines(false);
        tablaLibros.setSelectionBackground(new Color(67, 56, 202));
        tablaLibros.setSelectionForeground(Color.WHITE);
        tablaLibros.getTableHeader().setReorderingAllowed(false);
        sorter = new TableRowSorter<>(tablaModelo);
        tablaLibros.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tablaLibros);
        scrollPane.getViewport().setBackground(new Color(24, 24, 27));
        add(scrollPane, BorderLayout.CENTER);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrarTabla();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrarTabla();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrarTabla();
            }
        });

        JPanel panelInferior = new JPanel();
        panelInferior.setOpaque(false);
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));

        JPanel panelForm = new JPanel(new GridLayout(2, 6, 8, 8));
        panelForm.setOpaque(false);
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del libro"));

        txtId = new JTextField();
        txtNombre = new JTextField();
        txtAutor = new JTextField();
        txtEdicion = new JTextField();
        txtEditorial = new JTextField();
        txtAnio = new JTextField();

        panelForm.add(new JLabel("ID:"));
        panelForm.add(txtId);
        panelForm.add(new JLabel("Nombre:"));
        panelForm.add(txtNombre);
        panelForm.add(new JLabel("Autor:"));
        panelForm.add(txtAutor);
        panelForm.add(new JLabel("Edicion:"));
        panelForm.add(txtEdicion);
        panelForm.add(new JLabel("Editorial:"));
        panelForm.add(txtEditorial);
        panelForm.add(new JLabel("Anio:"));
        panelForm.add(txtAnio);

        panelInferior.add(panelForm);
        panelInferior.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelBotones.setOpaque(false);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> actualizarLibro());
        panelBotones.add(btnActualizar);

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(e -> eliminarLibro());
        panelBotones.add(btnEliminar);

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarCampos());
        panelBotones.add(btnLimpiar);

        boolean esCajero = usuarioLogueado.getRol() != Rol.ADMINISTRADOR;
        btnAgregar.setEnabled(!esCajero);
        btnActualizar.setEnabled(!esCajero);
        btnEliminar.setEnabled(!esCajero);
        txtId.setEnabled(!esCajero);
        txtNombre.setEnabled(!esCajero);
        txtAutor.setEnabled(!esCajero);
        txtEdicion.setEnabled(!esCajero);
        txtEditorial.setEnabled(!esCajero);
        txtAnio.setEnabled(!esCajero);

        if (esCajero) {
            JLabel lblRol = new JLabel("Solo el Administrador puede agregar, actualizar o eliminar libros.");
            lblRol.setForeground(new Color(248, 113, 113));
            panelBotones.add(lblRol);
        }

        panelInferior.add(panelBotones);
        add(panelInferior, BorderLayout.SOUTH);

        tablaLibros.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());
    }

    private void filtrarTabla() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
        }
    }

    public void buscar(String texto) {
        txtBuscar.setText(texto == null ? "" : texto.trim());
    }

    private void cargarSeleccion() {
        int filaVista = tablaLibros.getSelectedRow();
        if (filaVista >= 0) {
            int fila = tablaLibros.convertRowIndexToModel(filaVista);
            txtId.setText(tablaModelo.getValueAt(fila, 0).toString());
            txtNombre.setText(tablaModelo.getValueAt(fila, 1).toString());
            txtAutor.setText(tablaModelo.getValueAt(fila, 2).toString());
            txtEdicion.setText(tablaModelo.getValueAt(fila, 3).toString());
            txtEditorial.setText(tablaModelo.getValueAt(fila, 4).toString());
            txtAnio.setText(tablaModelo.getValueAt(fila, 5).toString());
        }
    }

    private void refrescarTabla() {
        tablaModelo.setRowCount(0);
        try {
            List<Libro> libros = gestionLibreria.consultarCatalogo(usuarioLogueado);
            for (Libro l : libros) {
                tablaModelo.addRow(new Object[]{
                        l.getId(), l.getNombreLibro(), l.getAutor(),
                        l.getEdicion(), l.getEditorial(), l.getAnioSalida()
                });
            }
        } catch (AccesoDenegadoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acceso denegado", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarLibro() {
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            String nombre = txtNombre.getText().trim();
            String autor = txtAutor.getText().trim();
            int edicion = Integer.parseInt(txtEdicion.getText().trim());
            String editorial = txtEditorial.getText().trim();
            int anio = Integer.parseInt(txtAnio.getText().trim());

            Libro libro = new Libro(id, nombre, autor, edicion, editorial, anio);
            gestionLibreria.agregarLibro(usuarioLogueado, libro);

            JOptionPane.showMessageDialog(this, "Libro agregado correctamente.", "Exito",
                    JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            refrescarTabla();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "ID, edicion y anio deben ser numeros validos.",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE);
        } catch (AccesoDenegadoException | LibroDuplicadoException | DatoInvalidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarLibro() {
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            String nombre = txtNombre.getText().trim();
            String autor = txtAutor.getText().trim();
            int edicion = Integer.parseInt(txtEdicion.getText().trim());
            String editorial = txtEditorial.getText().trim();
            int anio = Integer.parseInt(txtAnio.getText().trim());

            Libro libro = new Libro(id, nombre, autor, edicion, editorial, anio);
            gestionLibreria.actualizarLibro(usuarioLogueado, libro);

            JOptionPane.showMessageDialog(this, "Libro actualizado correctamente.", "Exito",
                    JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            refrescarTabla();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "ID, edicion y anio deben ser numeros validos.",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE);
        } catch (AccesoDenegadoException | LibroNoEncontradoException | DatoInvalidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarLibro() {
        int filaVista = tablaLibros.getSelectedRow();
        if (filaVista < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione el libro que desea eliminar en la tabla.",
                    "Seleccione un libro",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaLibros.convertRowIndexToModel(filaVista);
        int id = Integer.parseInt(tablaModelo.getValueAt(filaModelo, 0).toString());
        try {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Esta seguro de eliminar el libro con ID " + id + "?",
                    "Confirmar eliminacion",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            gestionLibreria.eliminarLibro(usuarioLogueado, id);

            JOptionPane.showMessageDialog(this, "Libro eliminado correctamente.", "Exito",
                    JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            refrescarTabla();
        } catch (AccesoDenegadoException | LibroNoEncontradoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtAutor.setText("");
        txtEdicion.setText("");
        txtEditorial.setText("");
        txtAnio.setText("");
        tablaLibros.clearSelection();
    }
}