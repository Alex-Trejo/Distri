# API Endpoints para el Microservicio de Publicaciones (`ms-publish`)

Este documento describe cómo interactuar con los endpoints del microservicio `ms-publicaciones` a través del `ms-api-gateway`. Todas las peticiones deben hacerse al `host` del API Gateway y seguir el patrón de enrutamiento configurado.

**URL Base del API Gateway:** `http://api-gateway.microservices.local`

---

## 1. Endpoints de Autores (`/autores`)

Estas rutas gestionan la creación, lectura, actualización y eliminación de autores.

**Ruta Base en API Gateway:** `/publicaciones/autores`

---

### Crear un Autor

Crea un nuevo autor en la base de datos.

*   **Método:** `POST`
*   **URL:** `http://api-gateway.microservices.local/publicaciones/autores`
*   **Headers:**
    *   `Content-Type: application/json`
*   **Cuerpo (Body) - Ejemplo:**
    ```json
    {
      "nombre": "Gabriel",
      "apellido": "García Márquez",
      "email": "gabo@example.com",
      "telefono": "123456789",
      "orcid": "0000-0002-1825-0097",
      "nacionalidad": "Colombiano",
      "institucion": "Independiente"
    }
    ```

---

### Listar todos los Autores

Obtiene una lista de todos los autores registrados.

*   **Método:** `GET`
*   **URL:** `http://api-gateway.microservices.local/publicaciones/autores`

---

### Obtener un Autor por ID

Busca y devuelve un autor específico por su ID.

*   **Método:** `GET`
*   **URL:** `http://api-gateway.microservices.local/publicaciones/autores/{id}`
    *   **Ejemplo:** `http://api-gateway.microservices.local/publicaciones/autores/1`

---

### Actualizar un Autor

Modifica la información de un autor existente.

*   **Método:** `PUT`
*   **URL:** `http://api-gateway.microservices.local/publicaciones/autores/actualizar/{id}`
    *   **Ejemplo:** `http://api-gateway.microservices.local/publicaciones/autores/actualizar/1`
*   **Headers:**
    *   `Content-Type: application/json`
*   **Cuerpo (Body) - Ejemplo:**
    ```json
    {
      "nombre": "Gabriel José",
      "apellido": "García Márquez",
      "email": "gabo.updated@example.com",
      "telefono": "987654321",
      "orcid": "0000-0002-1825-0097",
      "nacionalidad": "Colombiano",
      "institucion": "Premio Nobel"
    }
    ```

---

### Eliminar un Autor

Borra un autor de la base de datos por su ID.

*   **Método:** `DELETE`
*   **URL:** `http://api-gateway.microservices.local/publicaciones/autores/eliminar/{id}`
    *   **Ejemplo:** `http://api-gateway.microservices.local/publicaciones/autores/eliminar/1`

---

## 2. Endpoints de Libros (`/libros`)

Estas rutas gestionan los libros. **Nota:** Para crear o actualizar un libro, el `autorId` debe existir previamente.

**Ruta Base en API Gateway:** `/publicaciones/libros`

---

### Crear un Libro

Crea un nuevo libro asociado a un autor.

*   **Método:** `POST`
*   **URL:** `http://api-gateway.microservices.local/publicaciones/libros`
*   **Headers:**
    *   `Content-Type: application/json`
*   **Cuerpo (Body) - Ejemplo:**
    ```json
    {
      "titulo": "Cien años de soledad",
      "editorial": "Sudamericana",
      "anioPublicacion": 1967,
      "isbn": "978-0307474728",
      "resumen": "La historia de la familia Buendía en el pueblo de Macondo.",
      "autorId": 1,
      "genero": "Realismo mágico",
      "numPaginas": 417
    }
    ```

---

### Listar todos los Libros

Obtiene una lista de todos los libros.

*   **Método:** `GET`
*   **URL:** `http://api-gateway.microservices.local/publicaciones/libros`

---

### Obtener un Libro por ID

Busca y devuelve un libro específico por su ID.

*   **Método:** `GET`
*   **URL:** `http://api-gateway.microservices.local/publicaciones/libros/{id}`
    *   **Ejemplo:** `http://api-gateway.microservices.local/publicaciones/libros/1`

---

### Actualizar un Libro

Modifica la información de un libro existente.

*   **Método:** `PUT`
*   **URL:** `http://api-gateway.microservices.local/publicaciones/libros/actualizar/{id}`
    *   **Ejemplo:** `http://api-gateway.microservices.local/publicaciones/libros/actualizar/1`
*   **Headers:**
    *   `Content-Type: application/json`
*   **Cuerpo (Body) - Ejemplo:**
    ```json
    {
      "titulo": "Cien años de soledad (Edición Conmemorativa)",
      "editorial": "Alfaguara",
      "anioPublicacion": 2007,
      "isbn": "978-8420471839",
      "resumen": "La historia épica de la familia Buendía en el pueblo ficticio de Macondo.",
      "autorId": 1,
      "genero": "Realismo mágico",
      "numPaginas": 496
    }
    ```

---

### Eliminar un Libro

Borra un libro de la base de datos por su ID.

*   **Método:** `DELETE`
*   **URL:** `http://api-gateway.microservices.local/publicaciones/libros/eliminar/{id}`
    *   **Ejemplo:** `http://api-gateway.microservices.local/publicaciones/libros/eliminar/1`

---

## 3. Endpoints de Artículos Científicos (`/articulos`)

Estas rutas gestionan los artículos. **Nota:** Para crear un artículo, el `autorId` debe existir previamente.

**Ruta Base en API Gateway:** `/publicaciones/articulos`

---

### Crear un Artículo

Crea un nuevo artículo asociado a un autor.

*   **Método:** `POST`
*   **URL:** `http://api-gateway.microservices.local/publicaciones/articulos`
*   **Headers:**
    *   `Content-Type: application/json`
*   **Cuerpo (Body) - Ejemplo:**
    ```json
    {
      "titulo": "La estructura del ADN",
      "editorial": "Nature Publishing Group",
      "anioPublicacion": 1953,
      "isbn": "N/A",
      "resumen": "Un artículo que describe el descubrimiento de la estructura de doble hélice del ADN.",
      "autorId": 1,
      "revista": "Nature",
      "doi": "10.1038/171737a0",
      "areaInvestigacion": "Biología Molecular",
      "fechaPublicacion": "1953-04-25"
    }
    ```

---

### Listar todos los Artículos

Obtiene una lista de todos los artículos.

*   **Método:** `GET`
*   **URL:** `http://api-gateway.microservices.local/publicaciones/articulos`