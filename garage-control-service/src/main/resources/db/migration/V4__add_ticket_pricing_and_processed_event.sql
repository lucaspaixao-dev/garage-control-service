-- Pricing snapshot (captured at PARKED) and settlement (captured at EXIT) on the ticket.
ALTER TABLE ticket
    ADD COLUMN sector       VARCHAR(1),
    ADD COLUMN hourly_price NUMERIC(19, 2),
    ADD COLUMN fare         NUMERIC(19, 2),
    ADD COLUMN paid_at      TIMESTAMP;

-- Supports GET /revenue: sum of fares by sector over an exit-date range.
CREATE INDEX idx_ticket_sector_paid_at ON ticket (sector, paid_at);

-- Inbox/dedup table: makes vehicle-event processing idempotent against queue redeliveries.
CREATE TABLE processed_event (
    event_id     VARCHAR(128) PRIMARY KEY,
    processed_at TIMESTAMP    NOT NULL
);
