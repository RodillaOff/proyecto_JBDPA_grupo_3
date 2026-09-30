# Diagrama UML - Coliseum

```mermaid
classDiagram
    direction TB

    class JFrame
    class JDialog
    class JPanel
    class JButton
    class DocumentFilter

    class Coliseum {
        -notificacionesPanel NotificacionesPanel
        -irANotificaciones Runnable
        -avisoBandejaMostrado boolean
        -ventanaActiva$ Coliseum
        -loginActivo$ LoginDialog
        +Coliseum(usuarioActual)
        +main(args)$ void
        +iniciarInterfaz(salirSiCancela)$ void
        +mostrarOAbrir()$ void
        -traerAlFrente(ventana)$ void
        -ofrecerActivarNotificaciones() void
        -configurarSegundoPlano(usuarioActual) void
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
        -fraseActual String
        -fraseLabel JLabel
        -formatearFrase(frase) String
    }

    class CalendarioPanel {
        -mesActual YearMonth
        -usuarioId int
        -rutinasPorDia Map
        -rutinaSemanal Map
        -ejerciciosPorDia Map
        -horasPorDia Map
        +mostrarConfiguracionSemanalSiHaceFalta() void
        -mostrarConfiguracionSemanal() void
        -mostrarBlocDeNotas(dia) void
        -actualizarLegendSemanal() void
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

    class NotificacionesPanel {
        -usuarioId int
        -estadoLabel JLabel
        -detalleLabel JLabel
        +sinElegir() boolean
        +activar() void
        -desactivar() void
        -probar() void
        -abrirAjustes() void
        -actualizar() void
        -mostrarDetalle(windowsPermite) void
    }

    class FiltroHora {
        +replace(fb, offset, longitud, texto, attr) void
        +formatear(bruto)$ String
    }

    class Frases {
        +TODAS String[]
        +aleatoria()$ String
        +aleatoria(evitar)$ String
    }

    class Logo {
        -iconos List
        +aplicar(ventana)$ void
        +paraBandeja(tam)$ Image
        +escalar(img, lado)$ Image
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

    class Recordatorios {
        -planificador ScheduledExecutorService
        +iniciar(usuarioId)$ void
        +revisar(ahora, usuarioIdFiltro, simulacion)$ void
        +estaEnVentana(ahora, entreno, minutosAntes, ventanaMinutos)$ boolean
        +cuandoTexto(ahora, entreno)$ String
        +esDiaDeEntreno(texto)$ boolean
        +manejarArgumentos(args)$ boolean
        +tomarInstanciaUnica()$ boolean
        +lanzarServicioIndependiente()$ boolean
        +reiniciarAvisosEnMemoria()$ void
        +registrar(mensaje)$ void
        -planDeHoy(usuarioId, fecha)$ Plan
        -reclamar(usuarioId, fecha, canal)$ boolean
        -liberar(usuarioId, fecha, canal)$ void
        -limpiarAvisosViejos(antesDe)$ void
    }

    class Config {
        +horaPorDefecto LocalTime
        +minutosAntes int
        +ventanaMinutos int
        +cargar()$ Config
    }

    class Plan {
        +hora LocalTime
        +sesiones List
    }

    class Sesion {
        +titulo String
        +ejercicios String
    }

    class InstanciaUnica {
        +puerto()$ int
        +adquirir(alAbrir, pedirAbrir, esperaMs)$ boolean
        +cerrar()$ void
    }

    class AvisosWindows {
        +esWindows()$ boolean
        +notificacionesPermitidas()$ Boolean
        +parsearToastEnabled(salida)$ Boolean
        +abrirAjustesDeNotificaciones()$ boolean
    }

    class ToastWindows {
        +esWindows()$ boolean
        +mostrar(titulo, mensaje)$ boolean
    }

    class Bandeja {
        -icono TrayIcon
        +disponible()$ boolean
        +instalar(abrir, salir)$ boolean
        +avisar(titulo, mensaje)$ boolean
    }

    class ConexionBD {
        -asegurarEsquema()$ void
        +obtenerConexion()$ Connection
        +hashearContrasena(contrasena)$ String
        +normalizarHora(texto)$ String
        +validarCredenciales(usuario, pass)$ boolean
        +registrarUsuario(usuario, pass)$ boolean
        +existeUsuario(usuario)$ boolean
        +obtenerIdUsuario(nombre)$ int
        +obtenerNotificaciones(id)$ Boolean
        +guardarNotificaciones(id, activas)$ boolean
        +cargarResumenSemanal(id)$ Map
        +guardarResumenSemanal(id, resumenes)$ void
        +cargarHorasSemanal(id)$ Map
        +guardarRutinaSemanal(id, resumenes, horas)$ void
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
    JPanel <|-- NotificacionesPanel
    DocumentFilter <|-- FiltroHora

    Coliseum "1" *-- "1" InicioPanel : contiene
    Coliseum "1" *-- "1" CalendarioPanel : contiene
    Coliseum "1" *-- "1" RelojPanel : contiene
    Coliseum "1" *-- "1" CronometroPanel : contiene
    Coliseum "1" *-- "1" NotificacionesPanel : contiene
    Coliseum "1" *-- "5" BotonNav : sidebar

    Coliseum ..> LoginDialog : iniciarInterfaz() lo crea
    Coliseum ..> Tema : aplicar()
    Coliseum ..> InstanciaUnica : main() una sola instancia
    Coliseum ..> Recordatorios : segundo plano
    Coliseum ..> Bandeja : icono de bandeja
    Coliseum ..> Logo : icono de ventana

    LoginDialog ..> ConexionBD : login y registro
    CalendarioPanel ..> ConexionBD : rutinas, ejercicios y horas
    NotificacionesPanel ..> ConexionBD : preferencia de avisos

    InicioPanel ..> Frases
    CalendarioPanel ..> FiltroHora : campo de hora

    InicioPanel ..> BotonEstilo
    CalendarioPanel ..> BotonEstilo
    CronometroPanel ..> BotonEstilo
    LoginDialog ..> BotonEstilo
    NotificacionesPanel ..> BotonEstilo

    LoginDialog ..> Logo

    NotificacionesPanel ..> AvisosWindows : estado de Windows
    NotificacionesPanel ..> Bandeja : probar aviso

    Recordatorios ..> ConexionBD : usuarios, rutinas y avisos
    Recordatorios ..> Config : coliseum.properties
    Recordatorios ..> Frases : frase del aviso
    Recordatorios ..> Bandeja : mostrarAviso
    Recordatorios ..> InstanciaUnica : cerrar()
    Recordatorios "1" *-- "1" Plan : planDeHoy()
    Plan "1" *-- "*" Sesion

    Bandeja ..> ToastWindows : notificación de Windows
    Bandeja ..> Logo : icono

    ConexionBD ..> Recordatorios : reiniciarAvisosEnMemoria()

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
