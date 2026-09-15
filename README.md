# Backend - Banco 2026 (Laboratorio 01 - Arquitectura de Software)

Servicio backend RESTful desarrollado en Spring Boot para la gestión de cuentas bancarias, clientes y transferencias financieras con autenticación y autorización basada en roles.

---

## Tecnologías Utilizadas

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-HTTP_Basic-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![MapStruct](https://img.shields.io/badge/MapStruct-1.5.5-orange?style=for-the-badge)
![Lombok](https://img.shields.io/badge/Lombok-1.18.30-red?style=for-the-badge)
![Swagger / OpenAPI](https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

---

## Arquitectura y Estructura del Backend

El backend implementa una **Arquitectura en Capas (Layered Architecture)** siguiendo el patrón DTO-Mapper-Service-Repository:

```text
src/main/java/com/ejemplo/banco/banco2026/
├── config/             # Configuración de Seguridad (SecurityConfig) y OpenAPI
├── controller/         # Controladores REST (CustomerController, TransferController)
├── DTO/                # Objetos de Transferencia de Datos (CustomerDTO, TransactionDTO)
├── entity/             # Entidades JPA mapeadas a la base de datos MySQL (Customer, Transaction)
├── exception/          # Manejador global de excepciones (ResourceNotFoundException, etc.)
├── mapper/             # Mapeadores MapStruct entre Entidades y DTOs
├── repository/         # Interfaces de persistencia Spring Data JPA
└── service/            # Capa de lógica de negocio y transacciones
```

---

## Seguridad y Control de Acceso

La autenticación se gestiona mediante **HTTP Basic Authentication** con políticas de acceso basadas en roles:

| Rol | Usuario por Defecto | Contraseña | Permisos |
|---|---|---|---|
| **`ROLE_USER`** | `user` | `root` | Permiso de solo lectura en endpoints `GET` (`/api/**`). |
| **`ROLE_ADMIN`** | `admin` | `root` | Acceso completo: Lectura, Creación (`POST`), Actualización (`PUT`) y Eliminación (`DELETE`). |

- **Endpoints Públicos:** Documentación OpenAPI / Swagger (`/swagger-ui/**`, `/v3/api-docs/**`).

---

## Contrato de API REST

### 1. Clientes (`/api/customers`)

| Método | Endpoint | Rol Requerido | Descripción | Request Body | Response Body |
|---|---|---|---|---|---|
| `GET` | `/api/customers` | USER / ADMIN | Listar todos los clientes registrados | Ninguno | `List<CustomerDTO>` (200 OK) |
| `GET` | `/api/customers/{id}` | USER / ADMIN | Consultar cliente específico por ID | Ninguno | `CustomerDTO` (200 OK) |
| `POST` | `/api/customers` | ADMIN | Registrar un nuevo cliente | `CustomerDTO` | `CustomerDTO` (201 Created) |
| `PUT` | `/api/customers/{id}` | ADMIN | Actualizar información de un cliente | `CustomerDTO` | `CustomerDTO` (200 OK) |
| `DELETE` | `/api/customers/{id}` | ADMIN | Eliminar un cliente por ID | Ninguno | 204 No Content |

### 2. Transacciones (`/api/transactions`)

| Método | Endpoint | Rol Requerido | Descripción | Request Body | Response Body |
|---|---|---|---|---|---|
| `GET` | `/api/transactions` | USER / ADMIN | Listar todas las transacciones históricas | Ninguno | `List<TransactionDTO>` (200 OK) |
| `GET` | `/api/transactions/{id}` | USER / ADMIN | Consultar transacción por ID | Ninguno | `TransactionDTO` (200 OK) |
| `POST` | `/api/transactions` | ADMIN | Realizar transferencia de dinero | `TransactionDTO` | `TransactionDTO` (201 Created) |
| `PUT` | `/api/transactions/{id}` | ADMIN | Actualizar registro de transacción | `TransactionDTO` | `TransactionDTO` (200 OK) |
| `DELETE` | `/api/transactions/{id}` | ADMIN | Eliminar registro de transacción | Ninguno | 204 No Content |

---

## Requisitos Previos y Ejecución

### Requisitos
- **Java JDK 17** o superior instalado.
- **MySQL Server** ejecutándose en el puerto `3306`.
- Base de datos creada: `banco2026`.

### Configuración (`src/main/resources/application.properties`)
```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/banco2026?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=banco_user
spring.datasource.password=user123
spring.jpa.hibernate.ddl-auto=update
```

### Comandos de Ejecución

1. **Compilar el proyecto:**
   ```bash
   ./mvnw clean compile
   ```

2. **Iniciar la aplicación:**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Documentación Swagger UI:**
   Abrir en el navegador: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
