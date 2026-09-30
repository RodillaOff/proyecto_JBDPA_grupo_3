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
