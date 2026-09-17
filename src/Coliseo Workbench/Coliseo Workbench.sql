ALTER USER 'root'@'localhost' IDENTIFIED BY 'coliseo'; FLUSH PRIVILEGES;
-- Ejecutar completo en MySQL Workbench (rayo ⚡ o Ctrl+Shift+Enter)
CREATE DATABASE IF NOT EXISTS coliseum_db;
USE coliseum_db;

DROP TABLE IF EXISTS usuarios;

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(100) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(64) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);