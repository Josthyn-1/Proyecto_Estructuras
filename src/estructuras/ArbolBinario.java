package estructuras;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Binary search tree that keeps elements ordered according to its comparator.
 */
public class ArbolBinario<T> {

    private final Comparator<? super T> comparador;
    private NodoArbol<T> raiz;
    private int cantidad;

    public ArbolBinario(Comparator<? super T> comparador) {
        this.comparador = Objects.requireNonNull(comparador, "El comparador no puede ser nulo.");
    }

    public boolean insertar(T valor) {
        Objects.requireNonNull(valor, "El valor no puede ser nulo.");
        if (buscar(valor) != null) {
            return false;
        }
        raiz = insertar(raiz, valor);
        cantidad++;
        return true;
    }

    public T buscar(T valor) {
        Objects.requireNonNull(valor, "El valor no puede ser nulo.");
        NodoArbol<T> actual = raiz;
        while (actual != null) {
            int comparacion = comparador.compare(valor, actual.getValor());
            if (comparacion == 0) {
                return actual.getValor();
            }
            actual = comparacion < 0 ? actual.getIzquierdo() : actual.getDerecho();
        }
        return null;
    }

    public boolean eliminar(T valor) {
        Objects.requireNonNull(valor, "El valor no puede ser nulo.");
        if (buscar(valor) == null) {
            return false;
        }
        raiz = eliminar(raiz, valor);
        cantidad--;
        return true;
    }

    public List<T> recorridoInOrden() {
        List<T> valores = new ArrayList<>(cantidad);
        recorridoInOrden(raiz, valores);
        return valores;
    }

    public int tamano() {
        return cantidad;
    }

    private NodoArbol<T> insertar(NodoArbol<T> nodo, T valor) {
        if (nodo == null) {
            return new NodoArbol<>(valor);
        }
        if (comparador.compare(valor, nodo.getValor()) < 0) {
            nodo.setIzquierdo(insertar(nodo.getIzquierdo(), valor));
        } else {
            nodo.setDerecho(insertar(nodo.getDerecho(), valor));
        }
        return nodo;
    }

    private NodoArbol<T> eliminar(NodoArbol<T> nodo, T valor) {
        int comparacion = comparador.compare(valor, nodo.getValor());
        if (comparacion < 0) {
            nodo.setIzquierdo(eliminar(nodo.getIzquierdo(), valor));
        } else if (comparacion > 0) {
            nodo.setDerecho(eliminar(nodo.getDerecho(), valor));
        } else if (nodo.getIzquierdo() == null) {
            return nodo.getDerecho();
        } else if (nodo.getDerecho() == null) {
            return nodo.getIzquierdo();
        } else {
            NodoArbol<T> sucesor = minimo(nodo.getDerecho());
            nodo.setValor(sucesor.getValor());
            nodo.setDerecho(eliminar(nodo.getDerecho(), sucesor.getValor()));
        }
        return nodo;
    }

    private NodoArbol<T> minimo(NodoArbol<T> nodo) {
        NodoArbol<T> actual = nodo;
        while (actual.getIzquierdo() != null) {
            actual = actual.getIzquierdo();
        }
        return actual;
    }

    private void recorridoInOrden(NodoArbol<T> nodo, List<T> valores) {
        if (nodo != null) {
            recorridoInOrden(nodo.getIzquierdo(), valores);
            valores.add(nodo.getValor());
            recorridoInOrden(nodo.getDerecho(), valores);
        }
    }
}
