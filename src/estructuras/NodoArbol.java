package estructuras;

/**
 * Nodo de un árbol binario de búsqueda.
 */
public class NodoArbol<T> {

    private T valor;
    private NodoArbol<T> izquierdo;
    private NodoArbol<T> derecho;

    NodoArbol(T valor) {
        this.valor = valor;
    }

    public T getValor() {
        return valor;
    }

    public NodoArbol<T> getIzquierdo() {
        return izquierdo;
    }

    public NodoArbol<T> getDerecho() {
        return derecho;
    }

    void setValor(T valor) {
        this.valor = valor;
    }

    void setIzquierdo(NodoArbol<T> izquierdo) {
        this.izquierdo = izquierdo;
    }

    void setDerecho(NodoArbol<T> derecho) {
        this.derecho = derecho;
    }
}
