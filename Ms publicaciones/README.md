# Aplicaciones Distribuidas NRC 23316
# Despliegue Completo de Arquitectura de Microservicios en Minikube
### Trejo Alex
### Juan Pablo Pinza
### Karla Ansatuña

Este documento proporciona una guía paso a paso para desplegar una arquitectura completa de microservicios en un clúster local de Kubernetes gestionado por Minikube.

## Prerrequisitos

Antes de comenzar, asegúrate de tener instaladas las siguientes herramientas:
-   **Docker Desktop:** Configurado y en ejecución.
-   **Minikube:** Instalado.
-   **kubectl:** La CLI de Kubernetes, instalada.
-   **PowerShell o CMD** ejecutado como Administrador.

---

## Paso 1: Iniciar el Entorno de Minikube

Estos comandos preparan tu entorno de Kubernetes local.

1.  **Iniciar Minikube:**
    Este comando crea y arranca tu clúster de Kubernetes local usando Docker como controlador.
    ```bash
    minikube start --driver=docker
    ```

2.  **Habilitar el Addon Ingress:**
    El Ingress es necesario para exponer nuestros servicios a través de nombres de dominio.
    ```bash
    minikube addons enable ingress
    ```

3.  **Iniciar el Dashboard (Opcional):**
    Abre el Dashboard de Kubernetes en tu navegador para una vista gráfica del clúster.
    ```bash
    minikube dashboard
    ```

4.  **Iniciar el Túnel de Minikube (¡CRÍTICO!):**
    Este comando expone los servicios con una IP externa en tu máquina local. **Debe ejecutarse en una terminal separada y dejarse corriendo durante todo el proceso.**
    ```bash
    minikube tunnel
    ```

---

## Paso 2: Configuración del Archivo `hosts`

Para que los nombres de dominio de nuestros servicios funcionen en tu máquina, necesitas mapearlos a `localhost` (`127.0.0.1`), que es donde `minikube tunnel` expone los servicios.

1.  **Abre el archivo `hosts` como Administrador.**
    *   Puedes usar Notepad ejecutado como Administrador.
    *   Ruta del archivo: `C:\Windows\System32\drivers\etc\hosts`

2.  **Añade las siguientes líneas** al final del archivo y guárdalo:
    ```
    127.0.0.1 eureka.microservices.local
    127.0.0.1 rabbitmq-ui.microservices.local
    127.0.0.1 api-gateway.microservices.local
    ```

3.  **Limpia la caché DNS de Windows:**
    Abre una nueva terminal como Administrador y ejecuta:
    ```bash
    ipconfig /flushdns
    ```

---

## Paso 3: Despliegue de Servicios de Infraestructura

Ahora, desplegaremos los componentes base de nuestra arquitectura. Asegúrate de ejecutar los comandos `kubectl apply` desde la carpeta donde tienes guardados tus archivos `.yml`.

### 3.1 Despliegue: Eureka Server (`ms-eureka-server`)

El servidor de descubrimiento de servicios.

1.  **Aplicar manifiestos:**
    ```bash
    # Asume que los archivos están en una carpeta 'ms-eureka-server'
    kubectl apply -f ms-eureka-server/00-namespace.yml
    kubectl apply -f ms-eureka-server/01-eureka-deployment.yml
    kubectl apply -f ms-eureka-server/02-eureka-service.yml
    kubectl apply -f ms-eureka-server/03-eureka-ingress.yml
    ```

2.  **Verificación:**
    *   Espera a que el pod esté listo (`READY 1/1`):
        ```bash
        kubectl get pods -n microservices-ns -l app=eureka-server -w
        ```
    *   Accede al Dashboard de Eureka en tu navegador:
        `http://eureka.microservices.local`

### 3.2 Despliegue: Base de Datos (`postgresql`)

La base de datos para nuestros microservicios.

1.  **Aplicar manifiestos:**
    ```bash
    # Asume que los archivos están en una carpeta 'postgresql'
    kubectl apply -f postgresql/04-postgres-secrets.yml
    kubectl apply -f postgresql/05-postgres-pvc.yml
    kubectl apply -f postgresql/06-postgres-deployment-service.yml
    ```

2.  **Verificación:**
    *   Espera a que el pod de PostgreSQL esté listo (`READY 1/1`):
        ```bash
        kubectl get pods -n microservices-ns -l app=postgres-db -w
        ```

3.  **Ejecutar el Job de Inicialización:**
    *   Este Job crea todas las bases de datos necesarias (`authdb`, `db-publish`, etc.) de forma automática. **Ejecútalo solo después de que el pod de PostgreSQL esté listo.**
        ```bash
        kubectl apply -f postgresql/03-postgres-init-job.yml
        ```
    *   Verifica que el Job se complete:
        ```bash
        # Observa el pod del job hasta que su estado sea 'Completed'
        kubectl get pods -n microservices-ns -l job-name=postgres-db-init-job -w
        
        # Opcional: Revisa los logs para confirmar la creación de las bases de datos
        kubectl logs -n microservices-ns -l job-name=postgres-db-init-job
        ```

### 3.3 Despliegue: Cola de Mensajes (`rabbitmq`)

El broker de mensajería para comunicación asíncrona.

1.  **Aplicar manifiestos:**
    ```bash
    # Asume que los archivos están en una carpeta 'rabbitmq'
    kubectl apply -f rabbitmq/07-rabbitmq-secrets.yml
    kubectl apply -f rabbitmq/08-rabbitmq-pvc.yml
    kubectl apply -f rabbitmq/09-rabbitmq-deployment-service.yml
    kubectl apply -f rabbitmq/10-rabbitmq-ui-ingress.yml
    ```

2.  **Verificación:**
    *   Espera a que el pod esté listo (`READY 1/1`):
        ```bash
        kubectl get pods -n microservices-ns -l app=rabbitmq -w
        ```
    *   Accede a la Interfaz de Administración de RabbitMQ en tu navegador:
        `http://rabbitmq-ui.microservices.local`
        (Login: `admin` / `admin`)

---

## Paso 4: Despliegue de Microservicios de Aplicación

Con la infraestructura base desplegada, ahora levantamos los microservicios de nuestra aplicación.

### 4.1 Despliegue: API Gateway (`ms-api-gateway`)

El punto de entrada único para todas las peticiones.

1.  **Aplicar manifiestos:**
    ```bash
    kubectl apply -f ms-api-gateway/11-api-gateway-deployment.yml
    kubectl apply -f ms-api-gateway/12-api-gateway-service.yml
    kubectl apply -f ms-api-gateway/13-api-gateway-ingress.yml
    ```

2.  **Verificación:**
    ```bash
    kubectl get pods -n microservices-ns -l app=api-gateway -w
    ```

### 4.2 Despliegue: Servicio de Autenticación (`authservice`)

Gestiona la autenticación y generación de tokens JWT.

1.  **Aplicar manifiestos:**
    ```bash
    kubectl apply -f authservice/14-authservice-deployment.yml
    kubectl apply -f authservice/15-authservice-service.yml
    ```

2.  **Verificación:**
    ```bash
    kubectl get pods -n microservices-ns -l app=authservice -w
    ```

### 4.3 Despliegue: Servicio de Publicaciones (`ms-publish`)

Gestiona la creación de autores, libros y artículos.

1.  **Aplicar manifiestos:**
    ```bash
    kubectl apply -f ms-publish/16-publish-deployment.yml
    kubectl apply -f ms-publish/17-publish-service.yml
    ```

2.  **Verificación:**
    ```bash
    kubectl get pods -n microservices-ns -l app=ms-publish -w
    ```

### 4.4 Despliegue: Servicio de Catálogo (`ms-catalogo`)

Escucha eventos y crea un log de catálogo.

1.  **Aplicar manifiestos:**
    ```bash
    kubectl apply -f ms-catalogo/18-catalogo-deployment.yml
    kubectl apply -f ms-catalogo/19-catalogo-service.yml
    ```

2.  **Verificación:**
    ```bash
    kubectl get pods -n microservices-ns -l app=ms-catalogo -w
    ```

### 4.5 Despliegue: Servicio de Notificaciones (`ms-notificaciones`)

Escucha eventos y crea notificaciones.

1.  **Aplicar manifiestos:**
    ```bash
    kubectl apply -f ms-notificaciones/20-notificaciones-deployment.yml
    kubectl apply -f ms-notificaciones/21-notificaciones-service.yml
    ```

2.  **Verificación:**
    ```bash
    kubectl get pods -n microservices-ns -l app=ms-notificaciones -w
    ```

### 4.6 Despliegue: Servicio de Sincronización (`sync`)

Gestiona la sincronización de reloj entre los nodos.

1.  **Aplicar manifiestos:**
    ```bash
    kubectl apply -f ms-sync/22-sync-deployment.yml
    kubectl apply -f ms-sync/23-sync-service.yml
    ```

2.  **Verificación:**
    ```bash
    kubectl get pods -n microservices-ns -l app=ms-sync -w
    ```

---

## Paso 5: Verificación Funcional Completa

Ahora que todos los servicios están desplegados, podemos probar el flujo completo.

1.  **Verificar el Registro en Eureka:**
    *   Abre `http://eureka.microservices.local` en tu navegador. Deberías ver todos los servicios (`API-GATEWAY`, `AUTH-SERVICE`, `SERVICIO-PUBLICACIONES`, `SERVICIO-CATALOGO`, `SERVICIO-NOTIFICACIONES`) listados con estado `UP`.

2.  **Probar el Login para Obtener un Token:**
    *   Usa el siguiente comando `curl` en tu terminal para obtener un token JWT.

    **Para PowerShell / Linux / macOS:**
    ```bash
    curl -X POST -H "Content-Type: application/json" -d '{"username":"admin", "password":"admin123"}' http://api-gateway.microservices.local/api/auth/login
    ```

    **Para Windows CMD:**
    ```cmd
    curl -X POST -H "Content-Type: application/json" -d "{\"username\":\"admin\", \"password\":\"admin123\"}" http://api-gateway.microservices.local/api/auth/login
    ```
    *   **Respuesta esperada:** Un JSON con tu token: `{"token":"ey..."}`.

3.  **Probar el Flujo Asíncrono (RabbitMQ):**
    *   Crea un nuevo autor usando el API Gateway:
        ```bash
        curl -X POST -H "Content-Type: application/json" -d "{\"nombre\":\"Test\", \"apellido\":\"Autor\", \"email\":\"test.autor@example.com\", ...}" http://api-gateway.microservices.local/publicaciones/autores
        ```
    *   Revisa los logs de `ms-catalogo` y `ms-notificaciones` para confirmar que recibieron y procesaron el mensaje.
        ```bash
        # Reemplaza <nombre-pod> con el nombre real del pod
        kubectl logs -n microservices-ns <nombre-pod-catalogo>
        kubectl logs -n microservices-ns <nombre-pod-notificaciones>
        ```

¡Felicidades! Has desplegado una arquitectura de microservicios completa en Kubernetes.