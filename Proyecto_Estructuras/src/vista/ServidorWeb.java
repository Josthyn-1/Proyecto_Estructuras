package vista;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import modelo.Cliente;
import modelo.Libro;
import java.nio.file.Files;
import java.time.LocalDate;
import prestamos.Factura;
import prestamos.GestionPrestamos;
import prestamos.Prestamo;
import seguridad.GestionUsuarios;
import seguridad.Rol;
import seguridad.Usuario;
import servicios.GestionClientes;
import servicios.GestionLibreria;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Servidor HTTP local: sirve la carpeta web/ y expone la API JSON que usa la interfaz HTML. */
public class ServidorWeb {

    private final GestionUsuarios usuarios;
    private final GestionLibreria libreria;
    private final GestionPrestamos prestamos;
    private final GestionClientes clientes;
    private Usuario actual;
    private static final Path BASE = localizarBaseWeb();

    private static Path localizarBaseWeb() {
        Path proyecto = Path.of("").toAbsolutePath().normalize();
        Path[] candidatas = {
                proyecto.resolve("web"),
                proyecto.resolve("Jane").resolve("web"),
                proyecto.resolve("Proyecto_Estructuras").resolve("web"),
                proyecto.resolve("Proyecto_Estructuras").resolve("Jane").resolve("web")
        };
        for (Path candidata : candidatas) {
            if (Files.isRegularFile(candidata.resolve("index.html"))) {
                return candidata;
            }
        }
        throw new IllegalStateException(
                "No se encontró web/index.html. Ejecuta la aplicación desde la carpeta del proyecto.");
    }

    public ServidorWeb(GestionUsuarios u, GestionLibreria l, GestionPrestamos p, GestionClientes c) {
        this.usuarios = u; this.libreria = l; this.prestamos = p; this.clientes = c;
    }

    public void iniciar(int puerto) throws IOException {
        HttpServer s = HttpServer.create(new InetSocketAddress("localhost", puerto), 0);
        s.createContext("/", this::estatico);
        s.createContext("/api/", this::api);
        s.start();
    }

    private void estatico(HttpExchange ex) throws IOException {
        String ruta = ex.getRequestURI().getPath();
        if (ruta.equals("/")) ruta = "/index.html";
        Path f = BASE.resolve(ruta.substring(1)).normalize();
        if (!f.startsWith(BASE) || !Files.isRegularFile(f)) { enviar(ex, 404, "text/plain", "No encontrado"); return; }
        String tipo = ruta.endsWith(".html") ? "text/html; charset=utf-8" : ruta.endsWith(".css") ? "text/css; charset=utf-8"
                : ruta.endsWith(".js") ? "application/javascript; charset=utf-8" : ruta.endsWith(".png") ? "image/png"
                : ruta.matches(".*\\.jpe?g") ? "image/jpeg" : ruta.endsWith(".webp") ? "image/webp"
                : ruta.endsWith(".svg") ? "image/svg+xml" : "application/octet-stream";
        byte[] datos = Files.readAllBytes(f);
        if (datos.length > 3 && datos[0] == (byte) 0x89 && datos[1] == 'P') tipo = "image/png";
        enviar(ex, 200, tipo, datos);
    }

    private void api(HttpExchange ex) throws IOException {
        String ruta = ex.getRequestURI().getPath(), q = ex.getRequestURI().getQuery();
        String metodo = ex.getRequestMethod();
        byte[] raw = ex.getRequestBody().readAllBytes();
        String body = new String(raw, StandardCharsets.UTF_8);
        try {
            if (ruta.equals("/api/imagen")) {
                guardarImagen(q, raw);
                enviar(ex, 200, "application/json; charset=utf-8", "{}");
                return;
            }
            enviar(ex, 200, "application/json; charset=utf-8", ruta(ruta, metodo, body, q));
        } catch (RuntimeException e) {
            enviar(ex, 400, "application/json; charset=utf-8", "{\"error\":" + s(e.getMessage()) + "}");
        }
    }

    private void guardarImagen(String q, byte[] datos) throws IOException {
        if (actual == null || actual.getRol() != Rol.ADMINISTRADOR)
            throw new RuntimeException("Solo el administrador puede cambiar imágenes.");
        String slug = q == null ? "" : q.replace("slug=", "");
        if (!slug.matches("[a-z0-9-]{1,60}")) throw new RuntimeException("Categoría inválida.");
        if (datos.length == 0 || datos.length > 5_000_000) throw new RuntimeException("La imagen debe pesar menos de 5 MB.");
        Files.createDirectories(BASE.resolve("img"));
        Files.write(BASE.resolve("img").resolve(slug + ".jpg"), datos);
    }

    private static LocalDate fecha(String b, String k) {
        try { return LocalDate.parse(campo(b, k)); }
        catch (Exception e) { throw new RuntimeException("Fecha inválida en " + k + "."); }
    }

    private String ruta(String ruta, String metodo, String b, String q) {
        if (ruta.equals("/api/login")) {
            if (!usuarios.login(campo(b, "usuario"), campo(b, "clave")))
                throw new RuntimeException("Usuario o contraseña incorrectos.");
            actual = usuarios.getUsuarioAutenticado();
            return "{\"usuario\":" + s(actual.getUsername()) + ",\"rol\":" + s(actual.getRol().toString()) + "}";
        }
        if (ruta.equals("/api/logout")) { usuarios.logout(); actual = null; return "{}"; }
        if (actual == null) throw new RuntimeException("Sesión no iniciada.");

        switch (ruta) {
            case "/api/resumen": {
                double recaudo = prestamos.listarFacturas(actual).stream().filter(Factura::isPagada)
                        .mapToDouble(Factura::getMonto).sum();
                return "{\"libros\":" + libreria.cantidadLibros()
                        + ",\"activos\":" + prestamos.listarPrestamosActivos(actual).size()
                        + ",\"facturas\":" + prestamos.listarFacturas(actual).size()
                        + ",\"usuarios\":" + usuarios.getUsuarios().size()
                        + ",\"recaudo\":" + recaudo + "}";
            }
            case "/api/libros":
                if (metodo.equals("POST")) {
                    Libro nuevo = new Libro(num(b, "id"), campo(b, "nombre"), campo(b, "autor"),
                            num(b, "edicion"), campo(b, "editorial"), num(b, "anio"));
                    nuevo.setCategoria(campo(b, "categoria"));
                    libreria.agregarLibro(actual, nuevo);
                } else if (metodo.equals("DELETE")) {
                    libreria.eliminarLibro(actual, Integer.parseInt(q.replace("id=", "")));
                }
                return lista(libreria.consultarCatalogo(actual).stream().map(l -> "{\"id\":" + l.getId()
                        + ",\"nombre\":" + s(l.getNombreLibro()) + ",\"autor\":" + s(l.getAutor())
                        + ",\"edicion\":" + l.getEdicion() + ",\"editorial\":" + s(l.getEditorial())
                        + ",\"anio\":" + l.getAnioSalida() + ",\"categoria\":" + s(l.getCategoria()) + "}"));
            case "/api/cliente": {
                Cliente cl = clientes.buscar(q == null ? "" : q.replace("cedula=", ""));
                if (cl == null) throw new RuntimeException("No hay un cliente registrado con esa cédula.");
                return "{\"nombre\":" + s(cl.getNombreCompleto()) + ",\"bloqueado\":" + cl.isBloqueado()
                        + ",\"motivo\":" + s(cl.getMotivoBloqueo()) + "}";
            }
            case "/api/clientes": {
                if (metodo.equals("POST")) {
                    clientes.registrar(actual, new Cliente(campo(b, "cedula"), campo(b, "nombres"), campo(b, "apellidos")));
                } else if (metodo.equals("PUT")) {
                    Cliente cl = clientes.buscar(campo(b, "cedula"));
                    if (cl == null) throw new RuntimeException("Cliente no encontrado.");
                    if (Boolean.parseBoolean(campo(b, "bloqueado"))) cl.bloquear(campo(b, "motivo")); else cl.desbloquear();
                }
                List<Prestamo> activos = prestamos.listarPrestamosActivos(actual);
                return lista(clientes.listar().stream().map(x -> "{\"cedula\":" + s(x.getCedula())
                        + ",\"nombre\":" + s(x.getNombreCompleto()) + ",\"bloqueado\":" + x.isBloqueado()
                        + ",\"motivo\":" + s(x.getMotivoBloqueo()) + ",\"activos\":"
                        + activos.stream().filter(p -> p.getCliente().getCedula().equals(x.getCedula())).count() + "}"));
            }
            case "/api/devolver":
                return "{\"multa\":" + prestamos.registrarDevolucion(actual, campo(b, "id"),
                        campo(b, "fecha").isEmpty() ? LocalDate.now() : fecha(b, "fecha")) + "}";
            case "/api/disponibles":
                return lista(prestamos.listarLibrosDisponibles(actual).stream().map(l -> "{\"id\":" + l.getId()
                        + ",\"nombre\":" + s(l.getNombreLibro()) + "}"));
            case "/api/prestamos":
                if (metodo.equals("POST")) {
                    Cliente cli = clientes.buscar(campo(b, "cedula"));
                    if (cli == null)
                        throw new RuntimeException("No hay un cliente registrado con esa cédula. Regístralo primero en Clientes.");
                    prestamos.registrarPrestamo(actual, campo(b, "id").trim(), num(b, "libro"), cli,
                            fecha(b, "fecha"), fecha(b, "entrega"));
                }
                return lista(prestamos.listarHistorialPrestamos(actual).stream().map((Prestamo p) -> "{\"id\":"
                        + s(p.getIdPrestamo()) + ",\"libro\":" + s(p.getLibroPrestado().getNombreLibro())
                        + ",\"cliente\":" + s(p.getCliente().getNombreCompleto())
                        + ",\"fecha\":" + s(p.getFechaPrestamo().toString())
                        + ",\"entrega\":" + s(p.getFechaDevolucionEstimada().toString())
                        + ",\"mora\":" + prestamos.calcularMulta(p) + ",\"activo\":" + p.isActivo() + "}"));
            case "/api/facturas":
                if (metodo.equals("POST")) {
                    prestamos.generarFactura(actual, campo(b, "prestamo"));
                } else if (metodo.equals("PUT")) {
                    prestamos.modificarFactura(actual, campo(b, "id"), Double.parseDouble(campo(b, "monto")),
                            Boolean.parseBoolean(campo(b, "pagada")));
                } else if (metodo.equals("DELETE")) {
                    prestamos.eliminarFactura(actual, java.net.URLDecoder.decode(q.replace("id=", ""), StandardCharsets.UTF_8));
                }
                return lista(prestamos.listarFacturas(actual).stream().map((Factura f) -> "{\"id\":"
                        + s(f.getIdFactura()) + ",\"prestamo\":" + s(f.getIdPrestamo())
                        + ",\"cliente\":" + s(f.getNombreCliente()) + ",\"monto\":" + f.getMonto()
                        + ",\"fecha\":" + s(f.getFechaEmision().toString()) + ",\"pagada\":" + f.isPagada() + "}"));
            case "/api/usuarios":
                if (actual.getRol() != Rol.ADMINISTRADOR) throw new RuntimeException("Acceso denegado.");
                if (metodo.equals("POST")) {
                    usuarios.registrarUsuario(actual, new Usuario(campo(b, "usuario").trim(), campo(b, "clave"),
                            Rol.valueOf(campo(b, "rol"))));
                } else if (metodo.equals("DELETE")) {
                    usuarios.eliminarUsuario(actual, java.net.URLDecoder.decode(q.replace("usuario=", ""), StandardCharsets.UTF_8));
                }
                return lista(usuarios.getUsuarios().stream().map(u -> "{\"usuario\":" + s(u.getUsername())
                        + ",\"rol\":" + s(u.getRol().toString()) + "}"));
            default:
                throw new RuntimeException("Ruta desconocida.");
        }
    }

    private static String lista(java.util.stream.Stream<String> items) {
        return items.collect(Collectors.joining(",", "[", "]"));
    }

    private static String s(String t) {
        if (t == null) return "null";
        return "\"" + t.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "") + "\"";
    }

    private static String campo(String json, String k) {
        Matcher m = Pattern.compile("\"" + k + "\"\\s*:\\s*(?:\"((?:[^\"\\\\]|\\\\.)*)\"|([^,}\\s]+))").matcher(json);
        if (!m.find()) return "";
        return m.group(1) != null ? m.group(1).replace("\\\"", "\"").replace("\\\\", "\\") : m.group(2);
    }

    private static int num(String json, String k) {
        try { return Integer.parseInt(campo(json, k).trim()); }
        catch (NumberFormatException e) { throw new RuntimeException("El campo " + k + " debe ser numérico."); }
    }

    private static void enviar(HttpExchange ex, int codigo, String tipo, String cuerpo) throws IOException {
        enviar(ex, codigo, tipo, cuerpo.getBytes(StandardCharsets.UTF_8));
    }

    private static void enviar(HttpExchange ex, int codigo, String tipo, byte[] datos) throws IOException {
        ex.getResponseHeaders().set("Content-Type", tipo);
        ex.sendResponseHeaders(codigo, datos.length);
        ex.getResponseBody().write(datos);
        ex.close();
    }
}
