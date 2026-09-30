# Diagrama UML - Coliseum

```mermaid
classDiagram
    direction TB

    class JFrame {
        <<Swing>>
    }
    class JPanel {
        <<Swing>>
    }
    class JButton {
        <<Swing>>
    }
    class JDialog {
        <<Swing>>
    }

    class Coliseum {
        -actualizarSeleccion(botones: BotonNav[], activo: BotonNav) void
        +Coliseum(usuarioActual: String)
        +main(args: String[])$ void
    }

    class LoginDialog {
        -autenticado: boolean
        -usuarioActual: String
        -cardLayout: CardLayout
        -panelCentral: JPanel
        -campoUsuarioLogin: JTextField
        -campoContrasenaLogin: JPasswordField
        -mensajeLogin: JLabel
        -campoUsuarioRegistro: JTextField
        -campoContrasenaRegistro: JPasswordField
        -campoContrasenaRegistroConfirmar: JPasswordField
        -mensajeRegistro: JLabel
        ~LoginDialog(padre: Frame)
        -construirPanelLogin() JPanel
        -construirPanelRegistro() JPanel
        -crearEtiquetaCampo(texto: String) JLabel
        -estilizarCampo(campo: JTextField) void
        -intentarLogin() void
        -intentarRegistro() void
        -mostrarError(etiqueta: JLabel, texto: String) void
        ~isAutenticado() boolean
        ~getUsuarioActual() String
    }

    class BotonNav {
        -seleccionado: boolean
        -colorNormal: Color
        -colorHover: Color
        -colorSeleccionado: Color
        -textoNormal: Color
        -textoSeleccionado: Color
        ~BotonNav(texto: String)
        ~setSeleccionado(valor: boolean) void
    }

    class InicioPanel {
        -frases: String[]
        -fraseLabel: JLabel
        -random: Random
        +InicioPanel()
        -formatearFrase(frase: String) String
    }

    class CalendarioPanel {
        -mesActual: YearMonth
        -mesLabel: JLabel
        -diasPanel: JPanel
        -legendPanel: JPanel
        -usuarioId: int
        -rutinasPorDia: Map~LocalDate,List~String~~
        -rutinaSemanal: Map~DayOfWeek,String~
        -ejerciciosPorDia: Map~DayOfWeek,String~
        -rutinaSemanalConfigurada: boolean
        +CalendarioPanel(usuarioActual: String)
        -nombreDia(dia: DayOfWeek) String
        -actualizarLegendSemanal() void
        +mostrarConfiguracionSemanalSiHaceFalta() void
        -mostrarConfiguracionSemanal() void
        -mostrarBlocDeNotas(dia: DayOfWeek) void
        -actualizarCalendario() void
        -mostrarDialogoRutina(fecha: LocalDate) void
    }

    class RelojPanel {
        -horaLabel: JLabel
        -fechaLabel: JLabel
        +RelojPanel()
        -actualizarHora() void
    }

    class CronometroPanel {
        -tiempoInicio: long
        -tiempoAcumulado: long
        -corriendo: boolean
        -tiempoLabel: JLabel
        -timer: Timer
        +CronometroPanel()
        -actualizarTiempo() void
    }

    class Tema {
        <<utility>>
        +FONDO: Color$
        +FONDO_SECUNDARIO: Color$
        +FONDO_DIAS: Color$
        +BORDE_DIAS: Color$
        +TEXTO: Color$
        +TEXTO_SECUNDARIO: Color$
        +ACCENT: Color$
        +aplicar()$ void
    }

    class BotonEstilo {
        <<utility>>
        ~ACCENT: Color$
        ~TEXTO_OSCURO: Color$
        ~crear(texto: String)$ JButton
        ~crear(texto: String, colorFondo: Color, colorTexto: Color)$ JButton
    }

    class ConexionBD {
        <<utility>>
        -URL_DB: String$
        -USUARIO_DB: String$
        ~obtenerConexion()$ Connection
        ~hashearContrasena(contrasenaPlano: String)$ String
        ~validarCredenciales(usuario: String, contrasenaPlano: String)$ boolean
        ~existeUsuario(usuario: String)$ boolean
        ~registrarUsuario(usuario: String, contrasenaPlano: String)$ boolean
        ~obtenerIdUsuario(nombreUsuario: String)$ int
        ~cargarResumenSemanal(usuarioId: int)$ Map~DayOfWeek,String~
        ~cargarEjerciciosSemanal(usuarioId: int)$ Map~DayOfWeek,String~
        ~guardarResumenSemanal(usuarioId: int, resumenes: Map)$ void
        ~guardarEjerciciosDia(usuarioId: int, dia: DayOfWeek, ejercicios: String)$ void
        ~cargarRutinasDeFecha(usuarioId: int, fecha: LocalDate)$ List~String~
        ~cargarFechasConRutina(usuarioId: int, mes: YearMonth)$ Set~LocalDate~
        ~agregarRutinaDeFecha(usuarioId: int, fecha: LocalDate, descripcion: String)$ void
        ~eliminarRutinaDeFecha(usuarioId: int, fecha: LocalDate, descripcion: String)$ void
    }

    class MySQL {
        <<database>>
        coliseum_db
    }
    %% Herencia
  
¿Quién usa la aplicación?
El Usuario (o Deportista): Es la persona que entra a la app para organizar su entrenamiento del día a día. Usa el programa para armar su rutina de la semana, anotar recordatorios en días puntuales del calendario, tomarse los tiempos con el cronómetro y mantener su información guardada de forma segura

Funciones Principales
Crear una cuenta: Si es la primera vez que la persona usa la app, puede registrarse eligiendo un nombre de usuario y una contraseña. El sistema revisa al instante que ese nombre no esté ocupado por otra persona para evitar duplicados

Ingresar a la app (Login): Una vez registrado, el usuario pone sus datos para entrar. La app confirma que la contraseña sea correcta consultando la base de datos y le da la bienvenida a su panel personal

Organizar la rutina de la semana: Sirve para planificar el entrenamiento diario (de lunes a domingo). El usuario puede anotar qué ejercicios le tocan cada día y dejar un resumen rápido. Todo queda guardado automáticamente para cuando vuelva a entrar

Anotar notas en el calendario: Además de la rutina fija semanal, se pueden agregar notas puntuales para una fecha específica (por ejemplo, "Chequeo médico" o "Entrenamiento de fuerza el 15"). La app además resalta los días del mes que tienen notas guardadas para no pasarlas por alto

Usar el cronómetro: Un reloj digital pensado para medir los tiempos de descanso o de ejercicio. Muestra hasta las décimas de segundo y tiene tres botones simples: uno para empezar a contar, otro para pausar el tiempo sin perder lo recorrido y otro para reiniciar la cuenta a cero

¿Cómo funciona el inicio de sesión por dentro?
Cuando el usuario quiere entrar a su cuenta, el proceso pasa por estos pasos:

Escribe su usuario y clave en la pantalla de ingreso y toca "Iniciar Sesión" (o aprieta Enter)

Para cuidar su seguridad, el programa encripta la clave ingresada antes de hacer cualquier verificación

La app busca al usuario en la base de datos y revisa si la clave encriptada coincide con la que estaba guardada

Si todo está en orden, se cierra la ventana de ingreso y se abre la pantalla principal con todos sus datos y rutinas cargadas

¿Qué pasa si algo sale mal?

Si se olvida de llenar un campo: La app le avisa con un mensaje en rojo que debe completar tanto el usuario como la clave

Si le pifia a la contraseña: Aparece un aviso claro indicando que el usuario o la clave son incorrectos

Si falla la conexión a la base de datos: El sistema ataja el problema sin colgarse ni cerrarse de golpe, mostrándole un aviso de error comprensible al usuario
