Diagrama UML (Diagrama de Clases)

<pre>
+-------------------------------------------------------------+
|                        ConexionBD                           |
+-------------------------------------------------------------+
| - URL_DB : String                                           |
| - USUARIO_DB : String                                       |
| - CONTRASENA_DB : String                                    |
+-------------------------------------------------------------+
| ~ obtenerConexion() : Connection                            |
| ~ hashearContrasena(contrasenaPlano: String) : String       |
| ~ validarCredenciales(usuario: String, pass: String): boolean|
| ~ existeUsuario(usuario: String) : boolean                  |
| ~ registrarUsuario(usuario: String, pass: String) : boolean |
| ~ obtenerIdUsuario(nombreUsuario: String) : int             |
| ~ cargarResumenSemanal(usuarioId: int) : Map                |
| ~ cargarEjerciciosSemanal(usuarioId: int) : Map             |
| ~ guardarResumenSemanal(usuarioId: int, resumenes: Map)     |
| ~ guardarEjerciciosDia(usuarioId: int, dia: DayOfWeek, text: String) |
| ~ cargarRutinasDeFecha(usuarioId: int, fecha: LocalDate) : List&lt;String&gt; |
| ~ cargarFechasConRutina(usuarioId: int, mes: YearMonth) : Set&lt;LocalDate&gt; |
| ~ agregarRutinaDeFecha(usuarioId: int, fecha: LocalDate, desc: String) |
| ~ eliminarRutinaDeFecha(usuarioId: int, fecha: LocalDate, desc: String) |
+-------------------------------------------------------------+
                               ^
                               | (invoca métodos estáticos)
+------------------------------+------------------------------+
|                         LoginDialog                         |
+-------------------------------------------------------------+
| - autenticado : boolean                                     |
| - usuarioActual : String                                    |
| - cardLayout : CardLayout                                   |
| - panelCentral : JPanel                                     |
| - campoUsuarioLogin : JTextField                            |
| - campoContrasenaLogin : JPasswordField                     |
| - campoUsuarioRegistro : JTextField                         |
| - campoContrasenaRegistro : JPasswordField                  |
+-------------------------------------------------------------+
| + LoginDialog(padre: Frame)                                 |
| - construirPanelLogin() : JPanel                            |
| - construirPanelRegistro() : JPanel                         |
| - intentarLogin() : void                                    |
| - intentarRegistro() : void                                 |
| + isAutenticado() : boolean                                 |
| + getUsuarioActual() : String                               |
+-------------------------------------------------------------+

+-------------------------------------------------------------+
|                        BotonEstilo                          |
+-------------------------------------------------------------+
| ~ ACCENT : Color                                            |
| ~ TEXTO_OSCURO : Color                                      |
+-------------------------------------------------------------+
| ~ crear(texto: String) : JButton                            |
| ~ crear(texto: String, fondo: Color, textoCol: Color): JButton|
+-------------------------------------------------------------+
</pre>

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
