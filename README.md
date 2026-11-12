# Sistema de gestión de librería

Aplicación Java de escritorio para administrar libros, usuarios, préstamos y facturas.

## Estructuras de datos

El catálogo de libros usa tres índices coordinados en `servicios/GestionLibreria.java`:

- `estructuras/TablaHash.java`: tabla hash propia con encadenamiento separado y redimensionamiento. Busca los libros por ID en tiempo promedio O(1).
- `estructuras/ArbolBinario.java` y `estructuras/NodoArbol.java`: árbol binario de búsqueda ordenado por ID. El recorrido inorden muestra el catálogo ordenado y sus operaciones cuestan O(h), donde h es la altura del árbol.
- `TreeMap`: índice cronológico por año de publicación.

Las altas, actualizaciones y bajas del catálogo actualizan los índices para que las consultas por ID, por año y el recorrido ordenado reflejen los mismos libros.

## Ejecutar en VS Code

El código fuente está en `src` y el punto de entrada es `vista.Main`. El proyecto se puede abrir en VS Code con la extensión **Extension Pack for Java** y ejecutar desde la vista **JAVA PROJECTS**.

Credenciales iniciales: `admin` / `admin`.
