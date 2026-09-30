# Diagrama UML - Coliseum

```mermaid
classDiagram
    direction TB

    class JFrame
    class JDialog
    class JPanel
    class JButton

    class Coliseum {
        +Coliseum(usuarioActual)
        +main(args)$ void
        -actualizarSeleccion(botones, activo) void
    }

    class LoginDialog {
        -autenticado boolean
        -usuarioActual String
        -intentarLogin() void
        -intentarRegistro() void
        -construirPanelLogin() JPanel
        -construirPanelRegistro() JPanel
        +isAutenticado() boolean
        +getUsuarioActual() String
    }

    class BotonNav {
        -seleccionado boolean
        +setSeleccionado(valor) void
    }

    class InicioPanel {
        -frases String[]
        -fraseLabel JLabel
        -formatearFrase(frase) String
    }

    class CalendarioPanel {
        -mesActual YearMonth
        -usuarioId int
        -rutinasPorDia Map
        -rutinaSemanal Map
        -ejerciciosPorDia Map
        +mostrarConfiguracionSemanalSiHaceFalta() void
        -mostrarConfiguracionSemanal() void
        -mostrarBlocDeNotas(dia) void
        -actualizarCalendario() void
        -mostrarDialogoRutina(fecha) void
    }

    class RelojPanel {
        -horaLabel JLabel
        -fechaLabel JLabel
        -actualizarHora() void
    }

    class CronometroPanel {
        -tiempoInicio long
        -tiempoAcumulado long
        -corriendo boolean
        -timer Timer
        -actualizarTiempo() void
    }

    class BotonEstilo {
        +crear(texto)$ JButton
        +crear(texto, fondo, colorTexto)$ JButton
    }

    class Tema {
        +FONDO Color
        +FONDO_SECUNDARIO Color
        +FONDO_DIAS Color
        +TEXTO Color
        +ACCENT Color
        +aplicar()$ void
    }

    class ConexionBD {
        +obtenerConexion()$ Connection
        +validarCredenciales(usuario, pass)$ boolean
        +registrarUsuario(usuario, pass)$ boolean
        +existeUsuario(usuario)$ boolean
        +obtenerIdUsuario(nombre)$ int
        +cargarResumenSemanal(id)$ Map
        +guardarResumenSemanal(id, resumenes)$ void
        +cargarEjerciciosSemanal(id)$ Map
        +guardarEjerciciosDia(id, dia, texto)$ void
        +cargarRutinasDeFecha(id, fecha)$ List
        +cargarFechasConRutina(id, mes)$ Set
        +agregarRutinaDeFecha(id, fecha, desc)$ void
        +eliminarRutinaDeFecha(id, fecha, desc)$ void
    }

    class MySQL_coliseum_db

    JFrame <|-- Coliseum
    JDialog <|-- LoginDialog
    JButton <|-- BotonNav
    JPanel <|-- InicioPanel
    JPanel <|-- CalendarioPanel
    JPanel <|-- RelojPanel
    JPanel <|-- CronometroPanel

    Coliseum "1" *-- "1" InicioPanel : contiene
    Coliseum "1" *-- "1" CalendarioPanel : contiene
    Coliseum "1" *-- "1" RelojPanel : contiene
    Coliseum "1" *-- "1" CronometroPanel : contiene
    Coliseum "1" *-- "4" BotonNav : sidebar

    Coliseum ..> LoginDialog : main() lo crea
    Coliseum ..> Tema : aplicar()

    LoginDialog ..> ConexionBD : login y registro
    CalendarioPanel ..> ConexionBD : rutinas y ejercicios

    InicioPanel ..> BotonEstilo
    CalendarioPanel ..> BotonEstilo
    CronometroPanel ..> BotonEstilo
    LoginDialog ..> BotonEstilo

    BotonNav ..> Tema
    BotonEstilo ..> Tema

    ConexionBD ..> MySQL_coliseum_db : JDBC
  ```
-------------------------------------------------------------------
# CASOS DE USO

```mermaid
flowchart LR
    U([" Usuario"])
    BD[(" Base de datos")]

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
    end

    U --- UC1
    U --- UC2
    U --- UC3
    U --- UC4
    U --- UC5
    U --- UC6
    U --- UC7
    U --- UC8

    UC1 --- BD
    UC2 --- BD
    UC4 --- BD
    UC5 --- BD
    UC6 --- BD
```
