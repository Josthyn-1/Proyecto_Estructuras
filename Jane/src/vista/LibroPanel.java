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
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

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
        String[] columnas = {"ID", "Nombre", "Autor", "Edicion", "Editorial", "Anio"};
        tablaModelo = new DefaultTableModel(columnas, 0);
        tablaLibros = new JTable(tablaModelo);
        add(new JScrollPane(tablaLibros), BorderLayout.CENTER);

        JPanel panelForm = new JPanel(new GridLayout(2, 7, 5, 5));
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

        add(panelForm, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout());

        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.addActionListener(e -> agregarLibro());
        panelBotones.add(btnAgregar);

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
            JLabel lblRol = new JLabel("Modo lectura (Cajero) - no puede modificar el catalogo");
            lblRol.setForeground(Color.RED.darker());
            panelBotones.add(lblRol);
        }

        add(panelBotones, BorderLayout.SOUTH);

        tablaLibros.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());
    }

    private void cargarSeleccion() {
        int fila = tablaLibros.getSelectedRow();
        if (fila >= 0) {
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
        try {
            int id = Integer.parseInt(txtId.getText().trim());
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
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "ID debe ser un numero valido.",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE);
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