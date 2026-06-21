CREATE TABLE ticket (
    id            UUID         PRIMARY KEY,
    license_plate VARCHAR(16)  NOT NULL,
    spot_id       UUID         REFERENCES spot (id),
    status        VARCHAR(16)  NOT NULL
);


CREATE TABLE ticket_event (
    ticket_id  UUID        NOT NULL REFERENCES ticket (id),
    type       VARCHAR(16) NOT NULL,
    event_time TIMESTAMP,
    PRIMARY KEY (ticket_id, type)
);

-- Lookup: the open ticket of a plate (the ticket_event PK already indexes ticket_id).
CREATE INDEX idx_ticket_license_plate_status ON ticket (license_plate, status);
