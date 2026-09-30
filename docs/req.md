# Requerimientos - Coliseum

## Requisitos funcionales (RF)

### 1. Acceso de usuarios
- **RF-01 (Registro de usuarios):** el sistema debe permitir registrar un nuevo usuario con nombre de usuario (mínimo 3 caracteres) y contraseña (mínimo 4 caracteres) con confirmación.
- **RF-02 (Validación de duplicados):** el sistema debe verificar que el nombre de usuario no exista en la base de datos antes de registrarlo.
- **RF-03 (Inicio de sesión):** el sistema debe validar las credenciales comparando el hash de la contraseña ingresada con el almacenado en la base de datos.
- **RF-04 (Control de errores):** el sistema debe mostrar mensajes claros ante credenciales incorrectas, contraseñas que no coinciden, campos incompletos, datos demasiado cortos o fallos de conexión con la base de datos.
- **RF-05 (Acceso a la aplicación):** el sistema solo debe abrir la aplicación principal después de un inicio de sesión exitoso.

### 2. Inicio y navegación
- **RF-06 (Frases motivacionales):** el sistema debe mostrar una frase motivacional en la pantalla de inicio y permitir pedir una nueva. Las mismas frases se usan en las notificaciones de entreno.
- **RF-07 (Navegación):** el sistema debe ofrecer una barra lateral para cambiar entre Inicio, Calendario, Reloj, Cronómetro y Notificaciones.

### 3. Rutinas semanales y calendario
- **RF-08 (Resumen semanal):** el sistema debe permitir consultar y guardar un resumen corto de rutina para cada día de la semana. Al entrar al calendario por primera vez, sin rutina cargada, se ofrece configurarla.
- **RF-09 (Bloc de ejercicios diarios):** el sistema debe permitir cargar y guardar un bloc de notas con los ejercicios de cada día de la semana.
- **RF-10 (Rutinas por fecha):** el sistema debe permitir agregar y eliminar rutinas para una fecha concreta del calendario.
- **RF-11 (Días con rutina):** el sistema debe marcar en el calendario las fechas del mes que tienen al menos una rutina registrada.

### 4. Herramientas de entrenamiento
- **RF-12 (Reloj):** el sistema debe mostrar la hora y la fecha actuales, actualizadas cada segundo.
- **RF-13 (Cronómetro):** el sistema debe ofrecer un cronómetro con formato HH:mm:ss.S (horas, minutos, segundos y décimas).
- **RF-14 (Control del cronómetro):** el cronómetro debe permitir iniciar, pausar (manteniendo el tiempo acumulado) y reiniciar a cero.

### 5. Notificaciones de entreno
- **RF-15 (Hora de inicio por día):** el sistema debe permitir guardar, junto al resumen de cada día de la semana, la hora de inicio del entreno en formato HH:mm. El campo solo acepta números, agrega el ":" automáticamente, rechaza horas o minutos inválidos y, si se deja vacío, el día queda sin hora propia (usa la hora por defecto).
- **RF-16 (Activar y desactivar avisos):** el sistema debe ofrecer una pantalla «Notificaciones» donde cada usuario active o desactive sus avisos de entreno. La preferencia se guarda por usuario y puede estar sin elegir.
- **RF-17 (Oferta inicial):** al abrir la aplicación, si el usuario todavía no eligió, el sistema debe ofrecerle activar las notificaciones («Activar ahora» / «Más tarde»).
- **RF-18 (Aviso previo al entreno):** el sistema debe mostrar una notificación de escritorio unos minutos antes de la hora del entreno (20 por defecto), con el título «Tu entreno comienza en N minutos» y una frase motivacional. Solo se avisa en los días que tienen entreno: un día vacío o que diga Descanso, Libre, Off o No entreno no genera aviso. También cuentan las rutinas cargadas para una fecha concreta.
- **RF-19 (Sin avisos repetidos):** el sistema debe registrar los avisos ya enviados para no repetir la misma notificación el mismo día. Si se cambia una hora en la rutina semanal, el aviso vuelve a estar disponible.
- **RF-20 (Aviso tardío):** si la aplicación estaba cerrada a la hora programada, el sistema debe avisar igual dentro de una ventana de tolerancia (30 minutos por defecto), pero nunca una vez que el entreno ya empezó.
- **RF-21 (Prueba y ajustes de Windows):** el sistema debe permitir enviar una notificación de prueba, detectar si las notificaciones están desactivadas en Windows y abrir la configuración de Windows con las instrucciones para activarlas.
- **RF-22 (Configuración externa):** el sistema debe leer, desde el archivo opcional `coliseum.properties`, la hora por defecto (`recordatorio.hora_por_defecto`), los minutos de anticipación (`recordatorio.minutos_antes`) y la ventana de tolerancia (`recordatorio.ventana_minutos`). Los valores se releen sin reiniciar la aplicación.

### 6. Segundo plano y ventana
- **RF-23 (Segundo plano):** al cerrar la ventana con la X, la aplicación no debe terminar: se esconde en la bandeja del sistema y sigue avisando de los entrenos. Solo se cierra del todo con «Salir» en el icono de la bandeja.
- **RF-24 (Icono de bandeja):** el sistema debe mostrar un icono en la bandeja del sistema con las opciones para abrir la ventana y salir. Si el sistema no tiene bandeja, al cerrar se lanza el servicio de avisos como proceso independiente.
- **RF-25 (Instancia única):** el sistema debe permitir una sola instancia por usuario del sistema operativo. Si se abre de nuevo, la instancia existente muestra su ventana y el nuevo proceso termina sin pedir login otra vez.
- **RF-26 (Modos sin ventana):** el sistema debe ofrecer el modo `--recordatorios` (servicio de avisos para todos los usuarios, sin ventana) y el modo `--simular "AAAA-MM-DD HH:mm"` (muestra a quién se avisaría en ese momento, sin enviar nada).
- **RF-27 (Logo):** el sistema debe usar el logo de la aplicación (`logo.png`, si existe) en la ventana, el login, la barra de tareas y la bandeja.

## Requisitos no funcionales (RNF)

### 1. Seguridad
- **RNF-01 (Contraseñas cifradas):** las contraseñas nunca se guardan en texto plano; se transforman con SHA-256 a formato hexadecimal.
- **RNF-02 (Consultas seguras):** toda consulta a MySQL con datos del usuario debe usar `PreparedStatement` para prevenir inyección SQL. La única excepción es el script fijo que completa el esquema al arrancar, que no recibe datos externos.

### 2. Rendimiento y usabilidad
- **RNF-03 (Refresco del cronómetro):** el cronómetro debe actualizarse cada 50 ms con un `javax.swing.Timer`.
- **RNF-04 (Coherencia visual):** la interfaz debe usar una paleta centralizada (`Tema`) y estilos homogéneos (`BotonEstilo`), con respuesta visual al pasar el mouse (hover).

### 3. Mantenibilidad y arquitectura
- **RNF-05 (Liberación de recursos):** el acceso a datos debe usar `try-with-resources` para cerrar conexiones, consultas y `ResultSet`.
- **RNF-06 (Plataforma):** la aplicación debe ejecutarse en cualquier entorno Java 11 o superior con soporte para Swing.
- **RNF-07 (Base de datos):** la aplicación requiere un servidor MySQL en `localhost:3306` con la base `coliseum_db`.

### 4. Notificaciones y segundo plano
- **RNF-08 (Esquema autoactualizable):** al arrancar, la aplicación debe agregar por sí sola, si faltan, la columna `notificaciones` de `usuarios`, la columna `hora` de `rutina_semanal` y la tabla `avisos_entreno`. La operación es idempotente: se puede repetir en cada inicio.
- **RNF-09 (Revisión periódica):** un hilo de fondo (`coliseum-recordatorios`) debe revisar cada 30 segundos a quién corresponde avisar, sin bloquear la interfaz.
- **RNF-10 (Interfaz sin bloqueos):** la comprobación de los ajustes de Windows debe hacerse en un hilo aparte para no congelar la ventana.
- **RNF-11 (Notificación con identidad propia):** en Windows, la notificación debe mostrarse con el nombre «Coliseum» y el logo (mediante PowerShell). Si no es posible, se usa la notificación clásica de la bandeja de Java.
- **RNF-12 (Compatibilidad de notificaciones):** las notificaciones de escritorio requieren un sistema con bandeja del sistema; el estado de las notificaciones de Windows solo se consulta en Windows.
- **RNF-13 (Registro de eventos):** los avisos enviados, los reintentos, los diagnósticos y los errores deben quedar en el archivo `recordatorios.log`.
- **RNF-14 (Tolerancia a fallos):** si no se puede mostrar un aviso, se reintenta en la siguiente pasada dentro de la ventana; si falla el registro de avisos en la base, se usa un registro en memoria.
- **RNF-15 (Un solo servicio):** no puede haber dos servicios de avisos activos a la vez (bloqueo de archivo y puerto local propio de cada usuario, `-Dcoliseum.puerto` para forzarlo).
- **RNF-16 (Compatibilidad gráfica):** la aceleración por GPU de Java2D queda desactivada por defecto para evitar colores mal renderizados; se puede reactivar con `-Dcoliseum.gpu=true`.