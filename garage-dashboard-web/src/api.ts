export const API_BASE =
  (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? "http://localhost:8081";

export type SectorRevenue = { sector: string; amount: number };

export type Summary = {
  totalSpots: number;
  occupiedSpots: number;
  freeSpots: number;
  occupancyRate: number;
  revenueBySector: SectorRevenue[];
};

export type Spot = {
  externalId: number;
  sector: string | null;
  latitude: number;
  longitude: number;
  occupied: boolean;
};

export type TicketEvent = { type: string; time: string | null };

export type Ticket = {
  id: string;
  licensePlate: string;
  sector: string | null;
  status: string;
  hourlyPrice: number | null;
  fare: number | null;
  events: TicketEvent[];
};

export type Dashboard = { summary: Summary; spots: Spot[]; tickets: Ticket[] };

export type VehicleEvent = { type: string; licensePlate: string; at: string };

export async function fetchDashboard(signal?: AbortSignal): Promise<Dashboard> {
  const res = await fetch(`${API_BASE}/dashboard`, { signal });
  if (!res.ok) throw new Error(`GET /dashboard -> ${res.status}`);
  return res.json();
}

/**
 * Subscribes to the backend Server-Sent Events stream of vehicle events.
 * Returns an unsubscribe function.
 */
export function streamEvents(
  onEvent: (event: VehicleEvent) => void,
  onConnection: (open: boolean) => void,
): () => void {
  const source = new EventSource(`${API_BASE}/dashboard/stream`);

  source.addEventListener("open", () => onConnection(true));
  source.addEventListener("error", () => onConnection(false));
  source.addEventListener("vehicle-event", (event) => {
    try {
      onEvent(JSON.parse((event as MessageEvent).data) as VehicleEvent);
    } catch {
      /* ignore malformed frames */
    }
  });

  return () => source.close();
}
