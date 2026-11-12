package seguridad;

import excepciones.AccesoDenegadoException;
import excepciones.DatoInvalidoException;
import excepciones.UsuarioDuplicadoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gestiona los usuarios del sistema.
 * Se inicializa con un usuario Administrador por defecto
 * (username = "admin", password = "admin").
 * Solo un usuario autenticado con rol ADMINISTRADOR puede registrar nuevos usuarios.
 */
public class GestionUsuarios {

    private final List<Usuario> usuarios;
    private Usuario usuarioAutenticado;

    public GestionUsuarios() {
        this.usuarios = new ArrayList<>();
        // Usuario administrador por defecto con credenciales estrictas admin/admin
        this.usuarios.add(new Usuario("admin", "admin", Rol.ADMINISTRADOR));
    }

    /**
     * Autentica a un usuario. Guarda la sesión si las credenciales son correctas.
     *
     * @return true si la autenticación es exitosa, false en caso contrario.
     */
    public boolean login(String username, String password) {
        Optional<Usuario> encontrado = usuarios.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst();

        if (encontrado.isPresent()) {
            this.usuarioAutenticado = encontrado.get();
            return true;
        }
        this.usuarioAutenticado = null;
        return false;
    }

    public void logout() {
        this.usuarioAutenticado = null;
    }

    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param solicitante usuario autenticado que intenta realizar el registro.
     * @param nuevo       usuario a registrar.
     * @throws AccesoDenegadoException  si el solicitante no es autenticado o no es ADMINISTRADOR.
     * @throws UsuarioDuplicadoException si el username ya existe.
     */
    public void registrarUsuario(Usuario solicitante, Usuario nuevo) {
        if (solicitante == null || solicitante.getRol() != Rol.ADMINISTRADOR) {
            throw new AccesoDenegadoException(
                    "Acceso denegado: solo el Administrador puede registrar nuevos usuarios.");
        }

        if (usuarioExiste(nuevo.getUsername())) {
            throw new UsuarioDuplicadoException(
                    "El usuario '" + nuevo.getUsername() + "' ya existe en el sistema.");
        }

        usuarios.add(nuevo);
    }

    /**
     * Registra un nuevo usuario usando la sesión actualmente autenticada.
     *
     * @throws AccesoDenegadoException  si no hay sesión activa o el rol no es ADMINISTRADOR.
     * @throws UsuarioDuplicadoException si el username ya existe.
     */
    public void registrarUsuario(Usuario nuevo) {
        registrarUsuario(usuarioAutenticado, nuevo);
    }

    /**
     * Elimina un usuario del sistema. Solo un administrador puede hacerlo y no
     * puede eliminar su propia cuenta.
     */
    public void eliminarUsuario(Usuario solicitante, String username) {
        if (solicitante == null || solicitante.getRol() != Rol.ADMINISTRADOR) {
            throw new AccesoDenegadoException(
                    "Acceso denegado: solo el Administrador puede eliminar usuarios.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new DatoInvalidoException("Debe indicar el usuario que desea eliminar.");
        }
        if (solicitante.getUsername().equals(username)) {
            throw new AccesoDenegadoException("No puede eliminar su propia cuenta.");
        }

        Usuario usuario = buscarUsuario(username)
                .orElseThrow(() -> new DatoInvalidoException(
                        "No se encontró el usuario '" + username + "'."));
        usuarios.remove(usuario);
    }

    private boolean usuarioExiste(String username) {
        return usuarios.stream().anyMatch(u -> u.getUsername().equals(username));
    }

    public List<Usuario> getUsuarios() {
        return new ArrayList<>(usuarios);
    }

    public Optional<Usuario> buscarUsuario(String username) {
        return usuarios.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }
}
