import { useEffect, useMemo, useRef } from "react";
import type { Spot } from "../api";

function groupBySector(spots: Spot[]): [string, Spot[]][] {
  const groups = new Map<string, Spot[]>();
  for (const spot of spots) {
    const key = spot.sector ?? "?";
    const bucket = groups.get(key);
    if (bucket) bucket.push(spot);
    else groups.set(key, [spot]);
  }
  for (const bucket of groups.values()) bucket.sort((a, b) => a.externalId - b.externalId);
  return [...groups.entries()].sort((a, b) => a[0].localeCompare(b[0]));
}

export function SpotGrid({ spots }: { spots: Spot[] }) {
  // Track the previous occupancy so spots that just flipped get a brief highlight.
  const previous = useRef<Map<number, boolean>>(new Map());

  const changed = useMemo(() => {
    const set = new Set<number>();
    for (const spot of spots) {
      const was = previous.current.get(spot.externalId);
      if (was !== undefined && was !== spot.occupied) set.add(spot.externalId);
    }
    return set;
  }, [spots]);

  useEffect(() => {
    previous.current = new Map(spots.map((spot) => [spot.externalId, spot.occupied]));
  }, [spots]);

  const sectors = useMemo(() => groupBySector(spots), [spots]);

  return (
    <section className="rounded-2xl border border-white/5 bg-white/[0.02] p-5">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-300">Mapa de vagas</h2>
        <div className="flex items-center gap-4 text-xs text-slate-400">
          <span className="flex items-center gap-1.5">
            <span className="h-2.5 w-2.5 rounded-sm bg-emerald-500/40 ring-1 ring-emerald-400/40" /> livre
          </span>
          <span className="flex items-center gap-1.5">
            <span className="h-2.5 w-2.5 rounded-sm bg-rose-500" /> ocupada
          </span>
        </div>
      </div>

      <div className="space-y-5">
        {sectors.map(([sector, sectorSpots]) => {
          const occupied = sectorSpots.filter((s) => s.occupied).length;
          return (
            <div key={sector}>
              <div className="mb-2 flex items-center gap-2 text-xs text-slate-400">
                <span className="rounded-md bg-white/5 px-2 py-0.5 font-semibold text-slate-200">
                  Setor {sector}
                </span>
                <span className="tabular-nums">
                  {occupied}/{sectorSpots.length} ocupadas
                </span>
              </div>
              <div className="grid grid-cols-6 gap-2 sm:grid-cols-10 md:grid-cols-12">
                {sectorSpots.map((spot) => (
                  <div
                    key={spot.externalId}
                    title={`#${spot.externalId} · ${spot.latitude.toFixed(5)}, ${spot.longitude.toFixed(5)}`}
                    className={[
                      "flex aspect-square items-center justify-center rounded-md text-[10px] font-semibold tabular-nums transition-colors duration-500",
                      spot.occupied
                        ? "bg-rose-500/90 text-white shadow shadow-rose-500/20"
                        : "bg-emerald-500/10 text-emerald-300/80 ring-1 ring-emerald-500/25",
                      changed.has(spot.externalId) ? "ring-2 ring-amber-300" : "",
                    ].join(" ")}
                  >
                    {spot.externalId}
                  </div>
                ))}
              </div>
            </div>
          );
        })}
        {spots.length === 0 && (
          <p className="py-8 text-center text-sm text-slate-500">
            Nenhuma vaga carregada. Rode o setup das garagens (POST /garages).
          </p>
        )}
      </div>
    </section>
  );
}
