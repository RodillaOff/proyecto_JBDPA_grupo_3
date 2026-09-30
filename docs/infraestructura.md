# Infraestructura - Coliseum

## Diagrama

```mermaid
flowchart TB
    PC[" Computadora del usuario<br/>Windows"]

    subgraph JVM["Java (JVM)"]
        direction TB
        APP["Aplicación Coliseum<br/>Java Swing"]
        JDBC["Driver JDBC<br/>mysql-connector-j"]
    end

    subgraph SRV["Servidor local"]
        direction TB
        MYSQL["MySQL<br/>localhost:3306"]
        subgraph BD["Base de datos: coliseum_db"]
            direction TB
            T1[("usuarios")]
            T2[("rutina_semanal")]
            T3[("rutinas_dia")]
        end
    end

    PC --> APP
    APP -->|"consultas SQL"| JDBC
    JDBC -->|"conexión"| MYSQL
    MYSQL --> T1
    MYSQL --> T2
    MYSQL --> T3
```

## Modelo de datos

```mermaid
erDiagram
    usuarios ||--o{ rutina_semanal : "tiene"
    usuarios ||--o{ rutinas_dia : "tiene"

    usuarios {
        int id PK
        varchar nombre_usuario UK
        varchar contrasena_hash
        timestamp fecha_registro
    }

    rutina_semanal {
        int usuario_id PK, FK
        varchar dia_semana PK
        varchar resumen
        text ejercicios
    }

    rutinas_dia {
        int id PK
        int usuario_id FK
        date fecha
        varchar descripcion
    }
```

| Tabla | Para qué sirve |
|-------|----------------|
| `usuarios` | Usuario (único) y contraseña cifrada con SHA-256 |
| `rutina_semanal` | Resumen y ejercicios de cada día de la semana |
| `rutinas_dia` | Rutinas de una fecha concreta del calendario |

Si se borra un usuario, se borran también sus rutinas (`ON DELETE CASCADE`).

## Requisitos para ejecutarlo

1. Tener instalado Java (JDK).
2. Tener MySQL corriendo en el puerto 3306.
3. Ejecutar el script `Coliseo_Workbench.sql` en MySQL Workbench para crear la base `coliseum_db` y las tablas.
4. Tener `mysql-connector-j` en el classpath.
