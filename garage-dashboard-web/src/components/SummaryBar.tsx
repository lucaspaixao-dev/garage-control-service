import type { Summary } from "../api";
import { currency, percent } from "../format";

function Card({
  label,
  value,
  hint,
  accent,
}: {
  label: string;
  value: string;
  hint?: string;
  accent: string;
}) {
  return (
    <div className="rounded-2xl border border-white/5 bg-white/[0.03] p-4 shadow-lg shadow-black/20">
      <div className="text-xs font-medium uppercase tracking-wider text-slate-400">{label}</div>
      <div className={`mt-1 text-3xl font-semibold tabular-nums ${accent}`}>{value}</div>
      {hint && <div className="mt-1 text-xs text-slate-500">{hint}</div>}
    </div>
  );
}

export function SummaryBar({ summary }: { summary: Summary }) {
  return (
    <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
      <div className="rounded-2xl border border-white/5 bg-white/[0.03] p-4 shadow-lg shadow-black/20">
        <div className="flex items-center justify-between">
          <span className="text-xs font-medium uppercase tracking-wider text-slate-400">
            Ocupação
          </span>
          <span className="text-sm font-semibold tabular-nums text-slate-200">
            {percent(summary.occupancyRate)}
          </span>
        </div>
        <div className="mt-3 h-2.5 w-full overflow-hidden rounded-full bg-white/10">
          <div
            className="h-full rounded-full bg-gradient-to-r from-emerald-400 via-amber-400 to-rose-500 transition-[width] duration-700 ease-out"
            style={{ width: `${Math.min(100, Math.round(summary.occupancyRate * 100))}%` }}
          />
        </div>
        <div className="mt-2 text-xs text-slate-500">
          {summary.occupiedSpots} ocupadas · {summary.freeSpots} livres
        </div>
      </div>

      <Card
        label="Vagas totais"
        value={String(summary.totalSpots)}
        hint={`${summary.freeSpots} disponíveis agora`}
        accent="text-slate-100"
      />

      {summary.revenueBySector.map((sector) => (
        <Card
          key={sector.sector}
          label={`Receita hoje · setor ${sector.sector}`}
          value={currency(sector.amount)}
          accent="text-emerald-300"
        />
      ))}
    </div>
  );
}
