# Stack Tecnológico

- **Lenguaje de Programación**
  - Java (JDK 11+)
  - Lenguaje principal de desarrollo orientado a objetos

- **Interfaz Gráfica (GUI)**
  - Java Swing & AWT
  - Implementación de vistas (`JFrame`, `JDialog`, `JPanel`, `JButton`, `CardLayout`, `GridBagLayout`, `BoxLayout`)

- **Base de Datos**
  - MySQL 8.0+
  - Motor de base de datos relacional para la persistencia
  - MySQL Workbench para crear la base y consultar las tablas

- **Conector JDBC**
  - MySQL Connector/J (`com.mysql.cj.jdbc.Driver`)
  - Controlador oficial para la comunicación Java-MySQL

- **Seguridad / Cryptography**
  - `java.security.MessageDigest`
  - Generación de hashes SHA-256 para contraseñas
  - `PreparedStatement` para prevenir inyección SQL

- **Manejo de Fechas**
  - Java Time API (`LocalDate`, `LocalTime`, `LocalDateTime`, `DayOfWeek`, `YearMonth`)
  - Gestión estructurada de fechas, horas y rutinas del calendario

- **Notificaciones**
  - Notificaciones de Windows (toast) mostradas con PowerShell, con el nombre y el logo de Coliseum
  - `java.awt.SystemTray` y `TrayIcon` como icono en la bandeja y aviso de respaldo

- **Segundo plano**
  - `ScheduledExecutorService` para revisar los entrenos cada 30 segundos
  - `ServerSocket` local y `FileLock` para permitir una sola copia de la aplicación

- **Configuración y registros**
  - `java.util.Properties` para leer `coliseum.properties`
  - `recordatorios.log` para guardar avisos y errores
  - `logo.png` como imagen de la aplicación