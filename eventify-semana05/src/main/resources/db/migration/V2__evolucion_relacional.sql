-- V2: evolución relacional

-- 1) Venue gana "ciudad" (obligatoria)
ALTER TABLE venues ADD COLUMN ciudad VARCHAR(100) NOT NULL DEFAULT 'Sin definir';
ALTER TABLE venues ALTER COLUMN ciudad DROP DEFAULT;

-- 2) Event gana el flag de borrado lógico
ALTER TABLE events ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;

-- 3) Nueva tabla de categorías
CREATE TABLE categories (
                            id          BIGSERIAL    PRIMARY KEY,
                            nombre      VARCHAR(100) NOT NULL UNIQUE,
                            descripcion VARCHAR(300)
);

INSERT INTO categories (nombre, descripcion) VALUES
                                                 ('Conciertos',   'Presentaciones musicales en vivo'),
                                                 ('Talleres',     'Sesiones prácticas de aprendizaje'),
                                                 ('Conferencias', 'Charlas y ponencias de expertos'),
                                                 ('Deportes',     'Competencias y eventos deportivos'),
                                                 ('Gastronomía',  'Muestras y ferias gastronómicas'),
                                                 ('Festivales',   'Festivales culturales y de entretenimiento'),
                                                 ('Teatro',       'Obras y espectáculos escénicos'),
                                                 ('Rock',         'Música rock y subgéneros'),
                                                 ('Tecnología',   'Eventos de software, datos e innovación');

-- 4) Relación Many-to-One obligatoria Event -> Venue
--    Patrón seguro: columna nullable -> rellenar filas existentes -> NOT NULL.
--    (En una base limpia no hay filas, pero así la migración también serviría
--     sobre una base con datos reales.)
ALTER TABLE events ADD COLUMN venue_id BIGINT;
UPDATE events SET venue_id = (SELECT MIN(id) FROM venues) WHERE venue_id IS NULL;
ALTER TABLE events ALTER COLUMN venue_id SET NOT NULL;
ALTER TABLE events
    ADD CONSTRAINT fk_events_venue FOREIGN KEY (venue_id) REFERENCES venues (id);

-- 5) Tabla intermedia Many-to-Many Event <-> Category
CREATE TABLE events_categories (
                                   event_id    BIGINT NOT NULL,
                                   category_id BIGINT NOT NULL,
                                   PRIMARY KEY (event_id, category_id),
                                   CONSTRAINT fk_ec_event    FOREIGN KEY (event_id)    REFERENCES events (id),
                                   CONSTRAINT fk_ec_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- 6) Índices para las consultas del catálogo
CREATE INDEX idx_events_fecha    ON events (fecha DESC);
CREATE INDEX idx_events_venue_id ON events (venue_id);
CREATE INDEX idx_venues_ciudad   ON venues (ciudad);
CREATE INDEX idx_ec_category_id  ON events_categories (category_id);
