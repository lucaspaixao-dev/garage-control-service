# Garage Live Dashboard

Real-time web UI for the parking garage: it shows spots filling up / freeing in real time and
the tickets being created with their events, all live.

Built with **React 19 + TypeScript + Vite + Tailwind CSS v4**.

It talks to **`garage-control-dashboard-service`** (port `8081`), a read-only service that
queries the shared database for snapshots and consumes processed events from the
`dashboard-events` SQS queue to stream them live.

## How it works

- On load it fetches a full snapshot from `GET /dashboard` (summary, spots, tickets).
- It opens a **Server-Sent Events** stream at `GET /dashboard/stream`. Every vehicle event
  (ENTRY / PARKED / EXIT) pushed by the backend appears instantly in the activity feed and
  triggers an immediate snapshot refresh — so the spot map and ticket list update live.
- A 5s fallback poll keeps the UI fresh even if the stream drops.

## Running

Backend first (from the repo root): start the docker-compose stack (Postgres, LocalStack,
simulator), `garage-control-service` (port 8080) and `garage-control-dashboard-service`
(port 8081). Then:

```bash
cd garage-dashboard-web
npm install
npm run dev
```

Open http://localhost:5173.

To point at a different API host, copy `.env.example` to `.env` and set `VITE_API_BASE_URL`.

## Backend endpoints used (garage-control-dashboard-service · 8081)

| Endpoint | Purpose |
| --- | --- |
| `GET /dashboard` | Snapshot: summary + spots + recent tickets |
| `GET /dashboard/stream` | SSE stream of vehicle events (`vehicle-event`) |
