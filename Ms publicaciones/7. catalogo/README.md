# Verificación del Microservicio de Catálogo (`ms-catalogo`)

El microservicio `ms-catalogo` está diseñado para funcionar de manera reactiva, consumiendo eventos de una cola de mensajes en RabbitMQ. Su función principal es escuchar los eventos generados por otros servicios (como `ms-publish`) y crear un registro de auditoría o catálogo de estas actividades en su propia base de datos.

Debido a su naturaleza, la forma principal de verificar su funcionamiento no es a través de su API (aunque tiene una para consulta), sino observando su reacción a los eventos.

---

## Flujo de Verificación

El proceso para probar que `ms-catalogo` está operativo consiste en dos pasos principales:

1.  **Generar un Evento:** Realizar una acción en un microservicio productor (como `ms-publish`) que envíe un mensaje a la cola `catalog.cola`.
2.  **Verificar el Consumo:** Observar los logs del pod de `ms-catalogo` para confirmar que ha recibido, procesado y guardado el mensaje.

---

### Paso 1: Generar un Evento (Ejemplo: Crear un Autor)

Para que `ms-catalogo` tenga algo que procesar, primero debemos generar un evento. La forma más sencilla es crear una nueva entidad (como un Autor) a través del API Gateway, lo que hará que `ms-publish` envíe un mensaje a RabbitMQ.

1.  **Abre una terminal** (PowerShell, CMD, Bash) o una herramienta de API como Postman.

2.  **Asegúrate de tener los prerrequisitos:**
    *   El clúster de Minikube debe estar corriendo.
    *   La terminal con `minikube tunnel` debe estar activa.
    *   Tu archivo `hosts` debe contener la entrada para el API Gateway: `127.0.0.1 api-gateway.microservices.local`.

3.  **Envía una petición `POST` para crear un nuevo autor:**
    *   Usa el siguiente comando `curl`. Si estás en Windows CMD, asegúrate de usar las comillas dobles escapadas.

    **Para PowerShell / Linux / macOS:**
    ```bash
    curl -X POST \
      -H "Content-Type: application/json" \
      -d '{
        "nombre": "Jorge Luis",
        "apellido": "Borges",
        "email": "jorge.borges@example.com",
        "telefono": "5551234567",
        "orcid": "0000-0001-9999-8888",
        "nacionalidad": "Argentino",
        "institucion": "Biblioteca Nacional"
      }' \
      http://api-gateway.microservices.local/publicaciones/autores
    ```

    **Para Windows CMD:**
    ```cmd
    curl -X POST -H "Content-Type: application/json" -d "{\"nombre\":\"Jorge Luis\", \"apellido\":\"Borges\", \"email\":\"jorge.borges@example.com\", \"telefono\":\"5551234567\", \"orcid\":\"0000-0001-9999-8888\", \"nacionalidad\":\"Argentino\", \"institucion\":\"Biblioteca Nacional\"}" http://api-gateway.microservices.local/publicaciones/autores
    ```

    Si la petición es exitosa, recibirás una respuesta JSON confirmando la creación del autor.

---

### Paso 2: Verificar el Consumo del Evento en los Logs

Ahora que el mensaje ha sido enviado a RabbitMQ, `ms-catalogo` debería haberlo consumido casi instantáneamente. Podemos confirmarlo revisando los logs de su pod en Kubernetes.

1.  **Abre una nueva terminal.**

2.  **Obtén el nombre completo del pod `ms-catalogo`:**
    ```bash
    kubectl get pods -n microservices-ns -l app=ms-catalogo
    ```
    La salida será similar a:
    `NAME                           READY   STATUS    RESTARTS   AGE`
    `ms-catalogo-xxxxxxxxxx-yyyyy   1/1     Running   0          15m`
    Copia el nombre completo del pod.

3.  **Sigue los logs del pod en tiempo real:**
    Reemplaza `<NOMBRE_DEL_POD_DE_CATALOGO>` con el nombre que copiaste.
    ```bash
    kubectl logs -n microservices-ns <NOMBRE_DEL_POD_DE_CATALOGO> -f
    ```

4.  **Analiza la Salida del Log:**
    Inmediatamente después de haber creado el autor en el paso anterior, deberías ver una línea en los logs de `ms-catalogo` que confirma el procesamiento del mensaje. Esta línea es generada por el `CatalogListener`:

    ```
    Mensaje procesado y guardado: CatalogDto(mensaje=Título: Jorge Luis, Autor: Borges Jorge Luis, Resumen: jorge.borges@example.com, tipo=autor creado)
    ```
    Ver este mensaje es la confirmación de que:
    *   `ms-publish` envió el mensaje correctamente.
    *   RabbitMQ enrutó el mensaje a la cola `catalog.cola`.
    *   `ms-catalogo` se conectó a la cola, recibió el mensaje y su `CatalogListener` lo procesó con éxito.

---

### (Opcional) Verificación Adicional a través de la API de Catálogo

Para una doble confirmación, puedes consultar la API de `ms-catalogo` para ver si el registro del evento fue guardado en su base de datos.

1.  **Asegúrate de que el API Gateway tenga una ruta para `ms-catalogo`:**
    *   En `ms-api-gateway/application.yml`, debe existir la ruta:
        ```yaml
        - id: servicio-catalogo
          uri: lb://SERVICIO-CATALOGO
          predicates:
            - Path=/catalogo/**
          filters:
            - StripPrefix=1
        ```

2.  **Llama al endpoint de la API:**
    ```bash
    curl http://api-gateway.microservices.local/catalogo/catalog
    ```

3.  **Analiza la Salida:**
    La respuesta debería ser un array JSON que contenga todos los eventos procesados, incluyendo el de la creación del autor "Jorge Luis Borges".