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
CREATE TABLE IF NOT EXISTS rutina_semanal (
    usuario_id INT NOT NULL,
    dia_semana VARCHAR(10) NOT NULL,
    resumen VARCHAR(255) NOT NULL DEFAULT '',
    ejercicios TEXT,
    PRIMARY KEY (usuario_id, dia_semana),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS rutinas_dia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    fecha DATE NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
SELECT * FROM usuarios;
SELECT * FROM rutinas_dia;
SELECT * FROM rutina_semanal;