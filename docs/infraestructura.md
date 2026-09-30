# Infraestructura - Coliseum

## Diagrama

```mermaid
flowchart TB
    PC[" Computadora del usuario"]

    subgraph JVM["Java (JVM)"]
        direction TB
        APP["Aplicación Coliseum<br/>Java Swing"]
        REC["Recordatorios<br/>hilo de fondo, revisa cada 30 s"]
        BAN["Bandeja del sistema<br/>icono y avisos"]
        JDBC["Driver JDBC<br/>mysql-connector-j"]
    end

    subgraph ARCH["Archivos locales"]
        direction TB
        CFG["coliseum.properties<br/>(opcional)"]
        LOGO["logo.png<br/>(opcional)"]
        LOG["recordatorios.log"]
    end

    WIN["Windows<br/>notificaciones de escritorio"]

    subgraph SRV["Servidor local"]
        direction TB
        MYSQL["MySQL<br/>localhost:3306"]
        subgraph BD["Base de datos: coliseum_db"]
            direction TB
            T1[("usuarios")]
            T2[("rutina_semanal")]
            T3[("rutinas_dia")]
            T4[("avisos_entreno")]
        end
    end

    PC --> APP
    APP -->|"consultas SQL"| JDBC
    APP --> REC
    APP --> BAN
    REC -->|"consultas SQL"| JDBC
    REC -->|"aviso de entreno"| BAN
    BAN -->|"PowerShell"| WIN
    CFG -.->|"configura"| REC
    LOGO -.->|"icono"| BAN
    REC -.->|"registra"| LOG
    JDBC -->|"conexión"| MYSQL
    MYSQL --> T1
    MYSQL --> T2
    MYSQL --> T3
    MYSQL --> T4
```

## Modelo de datos

```mermaid
erDiagram
    usuarios ||--o{ rutina_semanal : "tiene"
    usuarios ||--o{ rutinas_dia : "tiene"
    usuarios ||--o{ avisos_entreno : "recibe"

    usuarios {
        int id PK
        varchar nombre_usuario UK
        varchar contrasena_hash
        timestamp fecha_registro
        tinyint notificaciones
    }

    rutina_semanal {
        int usuario_id PK, FK
        varchar dia_semana PK
        varchar resumen
        text ejercicios
        time hora
    }

    rutinas_dia {
        int id PK
        int usuario_id FK
        date fecha
        varchar descripcion
    }

    avisos_entreno {
        int usuario_id PK
        date fecha PK
        varchar canal PK
    }
```

| Tabla | Para qué sirve |
|-------|----------------|
| `usuarios` | Usuario (único), contraseña cifrada con SHA-256 y preferencia de notificaciones (sin elegir, activadas o desactivadas) |
| `rutina_semanal` | Resumen, ejercicios y hora de inicio de cada día de la semana |
| `rutinas_dia` | Rutinas de una fecha concreta del calendario |
| `avisos_entreno` | Registro de los avisos de entreno ya enviados, para no repetirlos (se limpian los de más de 7 días) |

Si se borra un usuario, se borran también sus rutinas (`ON DELETE CASCADE`). La tabla `avisos_entreno` no tiene clave foránea, así que sus registros no se borran en cascada.

La columna `notificaciones`, la columna `hora` y la tabla `avisos_entreno` no están en el script de Workbench: la aplicación las crea sola al arrancar si faltan.

## Requisitos para ejecutarlo

1. Tener instalado Java (JDK 11 o superior).
2. Tener MySQL corriendo en el puerto 3306.
3. Ejecutar el script `Coliseo_Workbench.sql` en MySQL Workbench para crear la base `coliseum_db` y las tablas.
4. Tener `mysql-connector-j` en el classpath.
5. Para las notificaciones de escritorio: un sistema con bandeja del sistema (recomendado Windows, con PowerShell y las notificaciones activadas en Configuración > Sistema > Notificaciones).
6. Opcional: `logo.png` junto al programa (icono de la ventana, la barra de tareas y la bandeja).
7. Opcional: `coliseum.properties` junto al programa para cambiar los avisos. Si no existe, se usan los valores por defecto.

| Propiedad | Por defecto | Para qué sirve |
|-----------|-------------|----------------|
| `recordatorio.hora_por_defecto` | `18:00` | Hora de entreno de los días que no tienen hora propia |
| `recordatorio.minutos_antes` | `20` | Minutos de anticipación del aviso |
| `recordatorio.ventana_minutos` | `30` | Tiempo de tolerancia para avisar si la app estaba cerrada |

## Modos de ejecución

| Comando | Qué hace |
|---------|----------|
| `java Coliseum` | Abre la aplicación con login. Si ya hay una instancia abierta, la trae al frente |
| `java Coliseum --recordatorios` | Servicio de avisos sin ventana para todos los usuarios |
| `java Coliseum --simular "2026-09-30 18:00"` | Muestra a quién se avisaría a esa fecha y hora, sin enviar nada |
| `java -Dcoliseum.gpu=true Coliseum` | Reactiva la aceleración gráfica por GPU (desactivada por defecto) |
| `java -Dcoliseum.puerto=NNNNN Coliseum` | Fuerza el puerto local usado para la instancia única |

## Archivos que genera

| Archivo | Para qué sirve |
|---------|----------------|
| `recordatorios.log` | Registro de avisos enviados, reintentos, diagnósticos y errores |
| `coliseum-recordatorios.lock` (carpeta temporal del sistema) | Evita que haya dos servicios de avisos a la vez |