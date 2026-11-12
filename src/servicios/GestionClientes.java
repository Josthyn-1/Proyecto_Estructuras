package servicios;

import estructuras.TablaHash;
import excepciones.AccesoDenegadoException;
import excepciones.DatoInvalidoException;
import modelo.Cliente;
import seguridad.Usuario;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Registro de clientes indexado por cédula en una TablaHash propia (búsqueda O(1) promedio). */
public class GestionClientes {

    private final TablaHash<String, Cliente> clientes = new TablaHash<>();

    public void registrar(Usuario solicitante, Cliente cliente) {
        if (solicitante == null) {
            throw new AccesoDenegadoException("Acceso denegado: se requiere autenticación.");
        }
        if (clientes.contieneClave(cliente.getCedula())) {
            throw new DatoInvalidoException("Ya existe un cliente con la cédula " + cliente.getCedula() + ".");
        }
        clientes.insertar(cliente.getCedula(), cliente);
    }

    public Cliente buscar(String cedula) {
        return clientes.obtener(cedula == null ? "" : cedula.trim());
    }

    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>(clientes.valores());
        lista.sort(Comparator.comparing(Cliente::getApellidos).thenComparing(Cliente::getNombres));
        return lista;
    }
}
