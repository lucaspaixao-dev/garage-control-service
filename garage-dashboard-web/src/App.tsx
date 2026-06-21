import { useCallback, useEffect, useRef, useState } from "react";
import { fetchDashboard, streamEvents } from "./api";
import type { Dashboard, VehicleEvent } from "./api";
import { clock } from "./format";
import { SummaryBar } from "./components/SummaryBar";
import { SpotGrid } from "./components/SpotGrid";
import { ActivityFeed } from "./components/ActivityFeed";
import { TicketsTable } from "./components/TicketsTable";

const FALLBACK_POLL_MS = 5000;
const REFRESH_DEBOUNCE_MS = 250;
const MAX_FEED = 40;

function ConnectionBadge({ online }: { online: boolean }) {
  return (
    <span
      className={[
        "inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-medium ring-1",
        online
          ? "bg-emerald-500/10 text-emerald-300 ring-emerald-500/30"
          : "bg-amber-500/10 text-amber-300 ring-amber-500/30",
      ].join(" ")}
    >
      <span
        className={[
          "h-2 w-2 rounded-full",
          online ? "bg-emerald-400 animate-pulse" : "bg-amber-400",
        ].join(" ")}
      />
      {online ? "Ao vivo" : "Reconectando…"}
    </span>
  );
}

export default function App() {
  const [data, setData] = useState<Dashboard | null>(null);
  const [feed, setFeed] = useState<VehicleEvent[]>([]);
  const [online, setOnline] = useState(false);
  const [updatedAt, setUpdatedAt] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    try {
      const snapshot = await fetchDashboard();
      setData(snapshot);
      setUpdatedAt(new Date().toISOString());
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    }
  }, []);

  const debounce = useRef<number | undefined>(undefined);

  useEffect(() => {
    void refresh();
    const poll = window.setInterval(() => void refresh(), FALLBACK_POLL_MS);

    const stop = streamEvents(
      (event) => {
        setFeed((current) => [event, ...current].slice(0, MAX_FEED));
        window.clearTimeout(debounce.current);
        debounce.current = window.setTimeout(() => void refresh(), REFRESH_DEBOUNCE_MS);
      },
      setOnline,
    );

    return () => {
      window.clearInterval(poll);
      window.clearTimeout(debounce.current);
      stop();
    };
  }, [refresh]);

  return (
    <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:py-8">
      <header className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">
            Garage <span className="text-sky-400">Live</span>
          </h1>
          <p className="text-sm text-slate-400">
            Vagas e tickets do estacionamento em tempo real
          </p>
        </div>
        <div className="flex items-center gap-3">
          {updatedAt && (
            <span className="text-xs text-slate-500">atualizado {clock(updatedAt)}</span>
          )}
          <ConnectionBadge online={online} />
        </div>
      </header>

      {error && (
        <div className="mb-4 rounded-xl border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-200">
          Não foi possível falar com a API ({error}). Verifique se o garage-control-dashboard-service
          está no ar em <code>{import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8081"}</code>.
        </div>
      )}

      {data ? (
        <div className="space-y-5">
          <SummaryBar summary={data.summary} />

          <div className="grid grid-cols-1 gap-5 lg:grid-cols-3">
            <div className="lg:col-span-2">
              <SpotGrid spots={data.spots} />
            </div>
            {/* relative + absolute child: the feed fills the spot-grid height and scrolls
                internally, instead of growing the row and pushing the tickets table down. */}
            <div className="relative h-[460px] lg:h-auto">
              <div className="lg:absolute lg:inset-0">
                <ActivityFeed events={feed} />
              </div>
            </div>
          </div>

          <TicketsTable tickets={data.tickets} />
        </div>
      ) : (
        !error && <p className="py-16 text-center text-slate-500">Carregando…</p>
      )}
    </div>
  );
}
