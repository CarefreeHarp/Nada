CREATE TABLE IF NOT EXISTS estudiantes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS materias (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    creditos INT NOT NULL CHECK (creditos > 0)
);

CREATE TABLE IF NOT EXISTS notas (
    id SERIAL PRIMARY KEY,
    valor DOUBLE PRECISION NOT NULL CHECK (valor >= 0 AND valor <= 5),
    porcentaje DOUBLE PRECISION NOT NULL CHECK (porcentaje >= 0 AND porcentaje <= 100),
    observacion TEXT,
    estudiante_id BIGINT NOT NULL,
    materia_id BIGINT NOT NULL,
    FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id) ON DELETE CASCADE,
    FOREIGN KEY (materia_id) REFERENCES materias(id) ON DELETE CASCADE
);
