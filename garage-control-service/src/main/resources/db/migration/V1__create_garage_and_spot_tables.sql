CREATE TABLE garage (
    id                     UUID           PRIMARY KEY,
    sector                 VARCHAR(1)     NOT NULL UNIQUE,
    base_price             NUMERIC(19, 2) NOT NULL,
    open_hour              TIME           NOT NULL,
    close_hour             TIME           NOT NULL,
    duration_limit_minutes INTEGER        NOT NULL
);

CREATE TABLE spot (
    id          UUID             PRIMARY KEY,
    external_id INTEGER          NOT NULL UNIQUE,
    garage_id   UUID             NOT NULL REFERENCES garage (id),
    latitude    DOUBLE PRECISION NOT NULL,
    longitude   DOUBLE PRECISION NOT NULL,
    occupied    BOOLEAN          NOT NULL
);

CREATE INDEX idx_spot_garage_id ON spot (garage_id);
