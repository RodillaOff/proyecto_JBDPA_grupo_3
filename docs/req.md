# Requerimientos - Coliseum

## Requisitos funcionales (RF)

### 1. Acceso de usuarios
- **RF-01 (Registro de usuarios):** el sistema debe permitir registrar un nuevo usuario con nombre de usuario (mínimo 3 caracteres) y contraseña (mínimo 4 caracteres) con confirmación.
- **RF-02 (Validación de duplicados):** el sistema debe verificar que el nombre de usuario no exista en la base de datos antes de registrarlo.
- **RF-03 (Inicio de sesión):** el sistema debe validar las credenciales comparando el hash de la contraseña ingresada con el almacenado en la base de datos.
- **RF-04 (Control de errores):** el sistema debe mostrar mensajes claros ante credenciales incorrectas, contraseñas que no coinciden, campos incompletos, datos demasiado cortos o fallos de conexión con la base de datos.
- **RF-05 (Acceso a la aplicación):** el sistema solo debe abrir la aplicación principal después de un inicio de sesión exitoso.

### 2. Inicio y navegación
- **RF-06 (Frases motivacionales):** el sistema debe mostrar una frase motivacional en la pantalla de inicio y permitir pedir una nueva.
- **RF-07 (Navegación):** el sistema debe ofrecer una barra lateral para cambiar entre Inicio, Calendario, Reloj y Cronómetro.

### 3. Rutinas semanales y calendario
- **RF-08 (Resumen semanal):** el sistema debe permitir consultar y guardar un resumen corto de rutina para cada día de la semana.
- **RF-09 (Bloc de ejercicios diarios):** el sistema debe permitir cargar y guardar un bloc de notas con los ejercicios de cada día de la semana.
- **RF-10 (Rutinas por fecha):** el sistema debe permitir agregar y eliminar rutinas para una fecha concreta del calendario.
- **RF-11 (Días con rutina):** el sistema debe marcar en el calendario las fechas del mes que tienen al menos una rutina registrada.

### 4. Herramientas de entrenamiento
- **RF-12 (Reloj):** el sistema debe mostrar la hora y la fecha actuales, actualizadas cada segundo.
- **RF-13 (Cronómetro):** el sistema debe ofrecer un cronómetro con formato HH:mm:ss.S (horas, minutos, segundos y décimas).
- **RF-14 (Control del cronómetro):** el cronómetro debe permitir iniciar, pausar (manteniendo el tiempo acumulado) y reiniciar a cero.

## Requisitos no funcionales (RNF)

### 1. Seguridad
- **RNF-01 (Contraseñas cifradas):** las contraseñas nunca se guardan en texto plano; se transforman con SHA-256 a formato hexadecimal.
- **RNF-02 (Consultas seguras):** toda consulta a MySQL debe usar `PreparedStatement` para prevenir inyección SQL.

### 2. Rendimiento y usabilidad
- **RNF-03 (Refresco del cronómetro):** el cronómetro debe actualizarse cada 50 ms con un `javax.swing.Timer`.
- **RNF-04 (Coherencia visual):** la interfaz debe usar una paleta centralizada (`Tema`) y estilos homogéneos (`BotonEstilo`), con respuesta visual al pasar el mouse (hover).

### 3. Mantenibilidad y arquitectura
- **RNF-05 (Liberación de recursos):** el acceso a datos debe usar `try-with-resources` para cerrar conexiones, consultas y `ResultSet`.
- **RNF-06 (Plataforma):** la aplicación debe ejecutarse en cualquier entorno Java 11 o superior con soporte para Swing.
- **RNF-07 (Base de datos):** la aplicación requiere un servidor MySQL en `localhost:3306` con la base `coliseum_db`.
