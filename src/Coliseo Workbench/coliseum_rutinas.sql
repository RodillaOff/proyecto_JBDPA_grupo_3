-- Ejecutar en MySQL Workbench, DESPUÉS de tener ya creada la tabla "usuarios"
-- (seleccioná solo este bloque y ejecutalo con el rayo ⚡, no re-corras el script viejo entero)

USE coliseum_db;

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
