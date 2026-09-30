RF-01 (Registro de Usuarios): El sistema debe permitir registrar un nuevo usuario especificando nombre de usuario y contraseña (con confirmación)

RF-02 (Validación de Duplicados): El sistema debe verificar que el nombre de usuario no exista previamente en la base de datos antes de registrarlo


RF-03 (Inicio de Sesión): El sistema debe validar las credenciales ingresadas comparando el hash de la contraseña ingresada con el almacenado en la base de datos

RF-04 (Control de Errores en Login/Registro): El sistema debe mostrar mensajes informativos claros ante fallos de autenticación, contraseñas no coincidentes o campos incompletos

2-Gestión de Rutinas Semanales y Calendario
RF-05 (Resumen Semanal): El sistema debe permitir consultar y guardar resúmenes cortos de rutina asignados a cada día de la semana (DayOfWeek)

RF-06 (Bloc de Ejercicios Diarios): El sistema debe permitir cargar y persistir un bloc de notas detallado con ejercicios específicos para cada día semanal

RF-07 (Notas Puntuales por Fecha): El sistema debe permitir agregar y eliminar notas específicas para una fecha concreta del calendario (LocalDate)

RF-08 (Visualización de Días con Rutina): El sistema debe marcar e identificar qué fechas de un mes determinado cuentan con al menos una nota o rutina registrada

3-Herramientas de Entrenamiento
RF-09 (Cronómetro de Precisión): El sistema debe proporcionar un cronómetro interactivo con formato de tiempo HH:mm:ss.S (horas, minutos, segundos y décimas)

RF-10 (Control de Tiempo): El cronómetro debe permitir Iniciar, Pausar (manteniendo el tiempo acumulado) y Reiniciar la cuenta a cero

Requisitos No Funcionales (RNF)
1-Seguridad
RNF-01 (Encriptación de Contraseñas): Las contraseñas en texto plano jamás deben almacenarse en la base de datos; deben ser transformadas mediante el algoritmo SHA-256 a formato hexadecimal

RNF-02 (Consultas Seguras): Toda interacción con MySQL debe emplear PreparedStatement para prevenir ataques de inyección SQL

2-Rendimiento y Usabilidad
RNF-03 (Tasa de Refresco de Interfaz): El cronómetro debe actualizar su visualización cada 50 ms mediante un javax.swing.Timer para garantizar fluidez visual

RNF-04 (Coherencia Visual / Look & Feel): La interfaz debe utilizar una paleta de colores centralizada (Tema) y estilos homogéneos (BotonEstilo) con respuesta visual a eventos del ratón (hover)

3-Mantenibilidad y Arquitectura
RNF-05 (Liberación de Recursos): La capa de acceso a datos debe usar try-with-resources para garantizar el cierre explícito de conexiones, consultas e instancias de ResultSet
RNF-06 (Independencia Plataforma): La aplicación debe ejecutarse sobre cualquier entorno runtime Java (JRE 8+) habilitado para Swing
