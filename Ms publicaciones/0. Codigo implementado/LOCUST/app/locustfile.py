from locust import HttpUser, task, between
import random
from datetime import datetime, timedelta
import string
import time

class PublicacionUser(HttpUser):
    # Asegúrate de que esta URL sea la correcta para tu API Gateway
    # Si tu API Gateway está en el host (y Locust en Docker Desktop):
    host = "http://host.docker.internal:8000"
    # Si tu API Gateway es otro servicio Docker en el mismo docker-compose:
    # host = "http://ms-api-gateway:8000"

    wait_time = between(0.5, 2.5) # Tiempo de espera entre tareas (entre 0.5 y 2.5 segundos)

    # Datos para generar autores
    nombres = ["Isabella", "Mateo", "Sofía", "Lucas", "Valentina", "Gabriel", "Camila", "Sebastián", "María", "Andrés"]
    apellidos = ["Reyes", "Gómez", "Fernández", "Díaz", "Ramírez", "Torres", "Mendoza", "Silva", "Morales", "Castillo"]

    # IDs de autores que ya existen en tu base de datos (actualiza esta lista si tienes más)
    EXISTING_AUTOR_IDS = [1, 5, 8, 10, 11]

    def generar_orcid(self):
        return f"0000-000{random.randint(0,9)}-{random.randint(1000,9999)}-{random.randint(1000,9999)}"

    def generar_email(self, nombre, apellido):
        sufijo = ''.join(random.choices(string.digits, k=4))
        return f"{nombre.lower()}.{apellido.lower()}{sufijo}@espe.edu.ec"

    def generar_telefono(self):
        return f"+5939{random.randint(10000000, 99999999)}"

    @task(3) # Prioridad 3: se ejecutará 3 veces más que get_articulos
    def create_articulo(self):
        titulos = [
            "Realismo mágico latinoamericano", "Análisis de la narrativa contemporánea",
            "Estudios literarios modernos", "Crítica literaria del siglo XXI",
            "Tendencias en la literatura hispanoamericana", "Nuevas perspectivas en estudios culturales",
            "Literatura y sociedad en América Latina"
        ]
        editoriales = [
            "Rev. Literaria", "Editorial Académica", "Publicaciones Universitarias",
            "Editorial Científica", "Revista de Estudios", "Editorial Latinoamericana"
        ]
        areas = [
            "Literatura", "Lingüística", "Estudios Culturales", "Crítica Literaria",
            "Análisis Textual", "Teoría Literaria"
        ]
        idiomas = ["Español", "Inglés", "Portugués", "Francés"]

        rand_num = random.randint(1, 100000) # Número más grande para mayor unicidad
        anio = random.randint(2015, 2024)

        start_date = datetime(anio, 1, 1)
        end_date = datetime(anio, 12, 31)
        random_date = start_date + timedelta(days=random.randint(0, (end_date - start_date).days))

        timestamp = int(time.time() * 1000)
        random_suffix = ''.join(random.choices(string.ascii_letters + string.digits, k=6)) # Sufijo más largo
        revista_unica = f"Revista_{random_suffix}_{timestamp}"
        doi_unico = f"10.{random.randint(1000,9999)}/{random_suffix.lower()}.{timestamp}"

        # Selecciona un autorId de los IDs existentes
        autor_id_to_use = random.choice(self.EXISTING_AUTOR_IDS)

        # Generar un ISBN si es necesario y no debe ser None
        isbn_val = "" # o f"ISBN-{random_suffix}-{timestamp}" if you need a value
        if random.random() > 0.5: # 50% chance to have a dummy ISBN
            isbn_val = f"978-{random.randint(100,999)}-{random.randint(1000,9999)}-{random.randint(100,999)}-{random.randint(0,9)}"

        payload = {
            "titulo": f"{random.choice(titulos)} - Estudio {rand_num}",
            "anioPublicacion": anio,
            "editorial": random.choice(editoriales),
            "isbn": isbn_val, # Usar el valor generado
            "resumen": f"Análisis detallado sobre {random.choice(areas).lower()} con enfoque en metodologías contemporáneas.",
            "idioma": random.choice(idiomas),
            "revista": revista_unica,
            "doi": doi_unico,
            "areaInvestigacion": random.choice(areas),
            "fechaPublicacion": random_date.strftime("%Y-%m-%d"),
            "autorId": autor_id_to_use # Usar un ID de autor válido
        }

        with self.client.post("/publicaciones/articulos", json=payload, catch_response=True) as response:
            if response.status_code == 201:
                response.success(f"Artículo creado: {payload['titulo']}")
            else:
                response.failure(f"Fallo al crear artículo ({response.status_code}): {response.text}")

    @task(1) # Prioridad 1: se ejecutará menos que create_articulo
    def get_articulos(self):
        with self.client.get("/publicaciones/articulos", catch_response=True) as response:
            if response.status_code != 200:
                response.failure(f"Fallo al obtener artículos: {response.text}")
            else:
                response.success()

    @task(2) # Prioridad 2: se ejecutará más que get_articulos pero menos que create_articulo
    def crear_autor(self):
        nombre = random.choice(self.nombres)
        apellido = random.choice(self.apellidos)

        payload = {
            "nombre": nombre,
            "apellido": apellido,
            "email": self.generar_email(nombre, apellido),
            "telefono": self.generar_telefono(),
            "orcid": self.generar_orcid(),
            "nacionalidad": "Ecuatoriana",
            "institucion": "Universidad de las Fuerzas Armadas ESPE"
        }

        with self.client.post("/publicaciones/autores", json=payload, catch_response=True) as response:
            if response.status_code == 201:
                response.success(f"Autor creado: {nombre} {apellido}")
            else:
                response.failure(f"Fallo al crear autor ({response.status_code}): {response.text}")