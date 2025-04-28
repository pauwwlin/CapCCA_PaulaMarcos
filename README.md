# Capgemini-Test

## Prerrequisitos

Asegúrese de tener instalados los siguientes componentes antes de ejecutar el proyecto:

- Java 17
- Maven
- Git
- Docker

---

## Ejecución del Docker para tener la infraestructura necesaria

1. Clone el repositorio:
   bash
   git clone <url-del-repositorio>
   
2. Navegue a la carpeta docker:
   bash
   cd docker
   
3. Levante los contenedores con Docker Compose:
   bash
   docker-compose up
   

### Contenedores del Docker

- *PostgreSQL:* Base de datos.
- *Mock-server:* API expuesta en el puerto 1080.

---

## APIs Expuestas en el Mock-server

### 1. API check-dni
- *Método:* PATCH
- *URL:* http://localhost:1080/check-dni
- *Body de la petición:*
  json
  {
    "dni": "<dni>"
  }
  
- *Respuestas:*
  - *OK:* Código HTTP 200 para cualquier DNI.
  - *KO:* Código HTTP 409 si el DNI es 99999999w.

---

### 2. API notification
#### Email
- *Método:* POST
- *URL:* http://localhost:1080/email
- *Body de la petición:*
  json
  {
    "email": "<email>",
    "message": "<msg>"
  }
  
- *Respuesta:*
  - *OK:* Código HTTP 200.

#### SMS
- *Método:* POST
- *URL:* http://localhost:1080/sms
- *Body de la petición:*
  json
  {
    "phone": "<phone>",
    "message": "<msg>"
  }
  
- *Respuesta:*
  - *OK:* Código HTTP 200.

---

## Contexto

La aplicación debe gestionar salas y usuarios bajo las siguientes condiciones:

- Cada sala tiene un ID único (long incremental).
- Una sala puede contener N usuarios.
- Un usuario puede estar en una única sala.
- Al guardar el usuario en la sala, se valida su DNI contra una API externa.
- Almacena el usuario en la base de datos y notifica según su rol.
- Devuelve el ID del usuario almacenado.

---

## Requisitos

### Validaciones:
1. *Nombre:* No debe contener más de 6 caracteres.
2. *Email:* Debe contener un @ y un ..
3. *Rol:* Sólo puede ser admin o superadmin.
4. Si el usuario ya existe (por email), se debe lanzar una excepción.
5. Validar el DNI contra la API externa del mock-server.

### Notificaciones:
- *Admin:* Notificación por email con el mensaje: "usuario guardado".
- *Superadmin:* Notificación por SMS con el mensaje: "usuario guardado".

### Respuesta esperada:
- *Retornar el ID generado.*

---

## Métodos a Implementar

### 1. Crear Usuario
- *Método:* POST
- *Descripción:* Guarda un usuario en la sala 1 y retorna su ID.
- *Ejemplo de JSON a guardar:*
  json
  {
    "name": "pablo",
    "email": "email@email.com",
    "phone": "677998899",
    "rol": "admin",
    "dni": "23454234W"
  }
  
- *Respuestas:*
  - *OK:* 
    - Código HTTP 201 Created
    - Body:
      json
      {
        "id": "<id>"
      }
      
  - *KO:* 
    - Código HTTP 409 Conflict
    - Body:
      json
      {
        "code": 409,
        "message": "error validation <email | userName | dni>"
      }
      

### 2. Obtener Usuario
- *Método:* GET
- *Descripción:* Obtiene un usuario basado en su ID dentro de la sala 1.

---

## Condiciones Opcionales

1. *Escalabilidad:* La aplicación podrá escalar para manejar diferentes contextos (pagos, pedidos, etc.).
2. *Capacidad:* La aplicación podrá procesar desde una hasta millones de peticiones (no es necesario implementar Kubernetes).

---

## Cosas que se Valoran

1. *Pruebas:*
   - Unitarias.
   - De integración.
   - De aceptación.
2. *Arquitectura e implementación.*
3. *Uso de Spring y abstracción del framework.*
4. *Patrones de diseño.*

---

## Método de Entrega

- Subir el proyecto a un repositorio público personal.
- Todos los commits deben estar realizados en la rama main.
