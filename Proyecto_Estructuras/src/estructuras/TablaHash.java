package estructuras;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Tabla hash con encadenamiento separado para resolver colisiones.
 */
public class TablaHash<K, V> {

    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA_MAXIMO = 0.75;

    private Entrada<K, V>[] cubetas;
    private int cantidad;

    public TablaHash() {
        cubetas = crearCubetas(CAPACIDAD_INICIAL);
    }

    public V obtener(K clave) {
        Entrada<K, V> entrada = buscarEntrada(clave);
        return entrada == null ? null : entrada.valor;
    }

    public boolean contieneClave(K clave) {
        return buscarEntrada(clave) != null;
    }

    public V insertar(K clave, V valor) {
        Objects.requireNonNull(clave, "La clave no puede ser nula.");
        Objects.requireNonNull(valor, "El valor no puede ser nulo.");

        int indice = indiceDe(clave, cubetas.length);
        for (Entrada<K, V> entrada = cubetas[indice]; entrada != null; entrada = entrada.siguiente) {
            if (entrada.clave.equals(clave)) {
                V valorAnterior = entrada.valor;
                entrada.valor = valor;
                return valorAnterior;
            }
        }

        cubetas[indice] = new Entrada<>(clave, valor, cubetas[indice]);
        cantidad++;
        if ((double) cantidad / cubetas.length > FACTOR_CARGA_MAXIMO) {
            redimensionar();
        }
        return null;
    }

    public V eliminar(K clave) {
        Objects.requireNonNull(clave, "La clave no puede ser nula.");
        int indice = indiceDe(clave, cubetas.length);
        Entrada<K, V> anterior = null;
        Entrada<K, V> actual = cubetas[indice];

        while (actual != null) {
            if (actual.clave.equals(clave)) {
                if (anterior == null) {
                    cubetas[indice] = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                cantidad--;
                return actual.valor;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return null;
    }

    public List<V> valores() {
        List<V> valores = new ArrayList<>(cantidad);
        for (Entrada<K, V> cubeta : cubetas) {
            for (Entrada<K, V> entrada = cubeta; entrada != null; entrada = entrada.siguiente) {
                valores.add(entrada.valor);
            }
        }
        return valores;
    }

    public int tamano() {
        return cantidad;
    }

    private Entrada<K, V> buscarEntrada(K clave) {
        Objects.requireNonNull(clave, "La clave no puede ser nula.");
        for (Entrada<K, V> entrada = cubetas[indiceDe(clave, cubetas.length)];
             entrada != null; entrada = entrada.siguiente) {
            if (entrada.clave.equals(clave)) {
                return entrada;
            }
        }
        return null;
    }

    private int indiceDe(K clave, int capacidad) {
        int hash = clave.hashCode();
        hash ^= hash >>> 16;
        return (hash & 0x7fffffff) % capacidad;
    }

    private void redimensionar() {
        Entrada<K, V>[] cubetasAnteriores = cubetas;
        cubetas = crearCubetas(cubetasAnteriores.length * 2);
        for (Entrada<K, V> cubeta : cubetasAnteriores) {
            for (Entrada<K, V> entrada = cubeta; entrada != null; entrada = entrada.siguiente) {
                int indice = indiceDe(entrada.clave, cubetas.length);
                cubetas[indice] = new Entrada<>(entrada.clave, entrada.valor, cubetas[indice]);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Entrada<K, V>[] crearCubetas(int capacidad) {
        return (Entrada<K, V>[]) new Entrada<?, ?>[capacidad];
    }

    private static final class Entrada<K, V> {
        private final K clave;
        private V valor;
        private Entrada<K, V> siguiente;

        private Entrada(K clave, V valor, Entrada<K, V> siguiente) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }
}
