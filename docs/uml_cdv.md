# Diagrama UML - Coliseum
```mermaid
classDiagram
    direction TB

    %% --- Capa de Presentación (UI) ---
    class Coliseum {
        +main(args)$
        +iniciarInterfaz()
    }
    class LoginDialog {
        +isAutenticado()
        +getUsuarioActual()
    }
    class InicioPanel
    class CalendarioPanel
    class RelojPanel
    class CronometroPanel
    class NotificacionesPanel

    %% --- Capa de Servicios y Configuración ---
    class Recordatorios {
        +iniciar(usuarioId)$
        +revisar()$
    }
    class Config {
        +cargar()$ Config
    }
    class InstanciaUnica {
        +adquirir()$ boolean
    }

    %% --- Capa de Datos ---
    class ConexionBD {
        +obtenerConexion()$
        +validarCredenciales()
        +cargarResumenSemanal()
        +guardarEjerciciosDia()
    }
    class MySQL_coliseum_db

    %% --- Sistema, Notificaciones y Apariencia ---
    class Bandeja {
        +instalar()$
        +avisar()$
    }
    class AvisosWindows
    class ToastWindows
    class Tema
    class Frases

    %% --- Relaciones UI ---
    Coliseum "1" *-- "1" InicioPanel
    Coliseum "1" *-- "1" CalendarioPanel
    Coliseum "1" *-- "1" RelojPanel
    Coliseum "1" *-- "1" CronometroPanel
    Coliseum "1" *-- "1" NotificacionesPanel

    Coliseum ..> LoginDialog : Autentica
    Coliseum ..> InstanciaUnica : Instancia única
    Coliseum ..> Recordatorios : Servicio fondo

    %% --- Relaciones Datos y Persistencia ---
    LoginDialog ..> ConexionBD
    CalendarioPanel ..> ConexionBD
    NotificacionesPanel ..> ConexionBD
    Recordatorios ..> ConexionBD
    ConexionBD ..> MySQL_coliseum_db : JDBC

    %% --- Relaciones Notificaciones ---
    Recordatorios ..> Config
    Recordatorios ..> Bandeja
    Bandeja ..> ToastWindows
    NotificacionesPanel ..> AvisosWindows

    %% --- Estilos y Utilidades ---
    InicioPanel ..> Frases
    Coliseum ..> Tema
    ```
-------------------------------------------------------------------
# CASOS DE USO

```mermaid
flowchart LR
    U([" Usuario"])
    BD[(" Base de datos")]
    WIN([" Windows"])

    subgraph SIS["Sistema Coliseum"]
        direction TB
        UC1(["Registrarse"])
        UC2(["Iniciar sesión"])
        UC3(["Ver frase motivacional"])
        UC4(["Ver calendario"])
        UC5(["Configurar rutina semanal"])
        UC6(["Agregar o quitar rutinas de un día"])
        UC7(["Ver reloj"])
        UC8(["Usar cronómetro"])
        UC9(["Activar o desactivar notificaciones"])
        UC10(["Probar notificación"])
        UC11(["Recibir aviso de entreno"])
        UC12(["Dejar la app en segundo plano"])
    end

    U --- UC1
    U --- UC2
    U --- UC3
    U --- UC4
    U --- UC5
    U --- UC6
    U --- UC7
    U --- UC8
    U --- UC9
    U --- UC10
    U --- UC11
    U --- UC12

    UC1 --- BD
    UC2 --- BD
    UC4 --- BD
    UC5 --- BD
    UC6 --- BD
    UC9 --- BD
    UC11 --- BD

    UC10 --- WIN
    UC11 --- WIN
```
