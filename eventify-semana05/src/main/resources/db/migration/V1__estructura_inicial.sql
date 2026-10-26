-- V1: estructura inicial (el estado de la semana 3)
CREATE TABLE venues (
                        id         BIGSERIAL    PRIMARY KEY,
                        nombre     VARCHAR(150) NOT NULL,
                        direccion  VARCHAR(250) NOT NULL,
                        capacidad  INTEGER      NOT NULL
);

CREATE TABLE events (
                        id          BIGSERIAL    PRIMARY KEY,
                        nombre      VARCHAR(150) NOT NULL,
                        fecha       DATE         NOT NULL,
                        descripcion VARCHAR(500)
);
