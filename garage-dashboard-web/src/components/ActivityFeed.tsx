import type { VehicleEvent } from "../api";
import { ago } from "../format";
import { eventStyle } from "../theme";

const LABEL: Record<string, string> = {
  ENTRY: "Entrou",
  PARKED: "Estacionou",
  EXIT: "Saiu",
};

export function ActivityFeed({ events }: { events: VehicleEvent[] }) {
  return (
    <section className="flex h-full flex-col rounded-2xl border border-white/5 bg-white/[0.02] p-5">
      <div className="mb-3 flex items-center justify-between">
        <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-300">
          Atividade ao vivo
        </h2>
        <span className="text-xs text-slate-500">{events.length} eventos</span>
      </div>

      <div className="-mr-2 flex-1 space-y-2 overflow-y-auto pr-2">
        {events.length === 0 && (
          <p className="py-8 text-center text-sm text-slate-500">Aguardando eventos…</p>
        )}
        {events.map((event, index) => (
          <div
            key={`${event.at}-${event.licensePlate}-${index}`}
            className="flash-in flex items-center gap-3 rounded-xl border border-white/5 bg-white/[0.02] px-3 py-2"
          >
            <span
              className={`rounded-md px-2 py-0.5 text-[11px] font-semibold ${eventStyle(event.type)}`}
            >
              {LABEL[event.type] ?? event.type}
            </span>
            <span className="font-mono text-sm text-slate-200">{event.licensePlate}</span>
            <span className="ml-auto text-xs text-slate-500">{ago(event.at)}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
