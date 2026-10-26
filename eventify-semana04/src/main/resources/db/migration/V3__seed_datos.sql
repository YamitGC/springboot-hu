-- V3: seed masivo (15 lugares + 200 eventos con categorías coherentes)

-- 1) Lugares (los 15 de la semana 3, ahora con ciudad)
INSERT INTO venues (nombre, direccion, ciudad, capacidad) VALUES
                                                              ('Centro de Convenciones Metropolitano', 'Av. El Sol 123',                  'Barranquilla', 1200),
                                                              ('Auditorio Innovación Tech',            'Calle 72 #45-10',                 'Barranquilla',  350),
                                                              ('Pabellón de Cristal',                  'Malecón del Río Sector 2',        'Barranquilla',  800),
                                                              ('Teatro Universitario Santander',       'Carrera 30 #45-03',               'Bucaramanga',   650),
                                                              ('Arena Bicentenario',                   'Diagonal 68 #22-15',              'Bogotá',       5000),
                                                              ('Espacio Coworking Hub Labs',           'Calle 100 #15-32 Piso 4',         'Bogotá',         80),
                                                              ('Hotel Grand Plaza Ballroom',           'Av. Circunvalar #88-20',          'Cartagena',     450),
                                                              ('Centro Cultural Las Bóvedas',          'Plaza Bolívar #4-12',             'Cartagena',     200),
                                                              ('Campus Tecnológico OpenSpace',         'Autopista Norte Km 14',           'Bogotá',        500),
                                                              ('Auditorio Central de Negocios',        'Calle 53 #10-60',                 'Medellín',      250),
                                                              ('Sala Multimedia Nexus',                'Carrera 15 #93-08',               'Bogotá',        120),
                                                              ('Parque Empresarial Auditorio A',       'Av. Esperanza #68B-85',           'Medellín',      300),
                                                              ('Estudio Creativo La Fábrica',          'Calle 4sur #43A-195',             'Medellín',      160),
                                                              ('Centro Empresarial Torre Norte',       'Carrera 7 #116-50',               'Bogotá',        180),
                                                              ('Coliseo Deportivo y Eventos',          'Av. Deportistas Transversal 12',  'Cali',         3500);

-- 2) 199 eventos generados con generate_series (nombres y fechas variados,
--    repartidos en round-robin entre los 15 lugares)
WITH v AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) - 1 AS rn FROM venues
)
INSERT INTO events (nombre, fecha, descripcion, activo, venue_id)
SELECT
    (ARRAY['Conferencia','Taller','Festival','Concierto','Torneo',
     'Feria','Foro','Cumbre','Encuentro','Gala'])[1 + (g % 10)]
    || ' ' ||
    (ARRAY['de Rock','de Java','de Innovación','de Gastronomía','de Teatro',
           'de Ciberseguridad','de Datos','de Emprendimiento','Deportivo','de Jazz'])[1 + ((g / 10) % 10)]
    || ' #' || g,
    DATE '2026-10-15' + (g * 2),
    'Evento de ejemplo número ' || g || ' generado por la migración V3',
    TRUE,
    (SELECT v.id FROM v WHERE v.rn = g % 15)
FROM generate_series(1, 199) AS g;

-- 3) Categorías COHERENTES con el nombre: una por el tipo de evento y otra por el tema
WITH tipo(prefijo, categoria) AS (VALUES
                                      ('Conferencia', 'Conferencias'), ('Taller',    'Talleres'),
                                      ('Festival',    'Festivales'),   ('Concierto', 'Conciertos'),
                                      ('Torneo',      'Deportes'),     ('Feria',     'Gastronomía'),
                                      ('Foro',        'Conferencias'), ('Cumbre',    'Tecnología'),
                                      ('Encuentro',   'Conferencias'), ('Gala',      'Teatro')),
     tema(sufijo, categoria) AS (VALUES
                                     ('de Rock',          'Rock'),         ('de Java',      'Tecnología'),
                                     ('de Innovación',    'Tecnología'),   ('de Gastronomía','Gastronomía'),
                                     ('de Teatro',        'Teatro'),       ('de Ciberseguridad','Tecnología'),
                                     ('de Datos',         'Tecnología'),   ('de Emprendimiento','Conferencias'),
                                     ('Deportivo',        'Deportes'),     ('de Jazz',      'Conciertos'))
INSERT INTO events_categories (event_id, category_id)
SELECT e.id, c.id
FROM events e
         JOIN tipo t       ON e.nombre LIKE t.prefijo || ' %'
         JOIN categories c ON c.nombre = t.categoria
UNION                                   -- UNION (no UNION ALL) evita pares duplicados
SELECT e.id, c.id
FROM events e
         JOIN tema m       ON e.nombre LIKE '% ' || m.sufijo || ' #%'
         JOIN categories c ON c.nombre = m.categoria;

-- 4) Evento especial para probar la búsqueda parcial e insensible (Escenario 6)
INSERT INTO events (nombre, fecha, descripcion, activo, venue_id)
VALUES ('Concierto de ROCK', DATE '2026-12-20', 'Gran concierto de rock en vivo', TRUE,
        (SELECT id FROM venues WHERE nombre = 'Arena Bicentenario'));   -- Bogotá

INSERT INTO events_categories (event_id, category_id)
SELECT e.id, c.id
FROM events e, categories c
WHERE e.nombre = 'Concierto de ROCK' AND c.nombre IN ('Rock', 'Conciertos');
