import type { Ticket } from "../api";
import { clock, currency } from "../format";
import { dotStyle, statusStyle } from "../theme";

const ORDER = ["ENTRY", "PARKED", "EXIT"];

function Timeline({ ticket }: { ticket: Ticket }) {
  const present = new Set(ticket.events.map((event) => event.type));
  return (
    <div className="flex items-center gap-1">
      {ORDER.map((type) => (
        <span
          key={type}
          title={type}
          className={[
            "h-2 w-2 rounded-full transition-colors",
            present.has(type) ? dotStyle(type) : "bg-white/10",
          ].join(" ")}
        />
      ))}
    </div>
  );
}

export function TicketsTable({ tickets }: { tickets: Ticket[] }) {
  return (
    <section className="rounded-2xl border border-white/5 bg-white/[0.02] p-5">
      <div className="mb-3 flex items-center justify-between">
        <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-300">
          Tickets recentes
        </h2>
        <span className="text-xs text-slate-500">{tickets.length} tickets</span>
      </div>

      <div className="-mr-2 max-h-[420px] overflow-y-auto pr-2">
        <table className="w-full border-separate border-spacing-y-1 text-sm">
          <thead className="text-left text-xs uppercase tracking-wider text-slate-500">
            <tr>
              <th className="px-3 py-1 font-medium">Placa</th>
              <th className="px-3 py-1 font-medium">Setor</th>
              <th className="px-3 py-1 font-medium">Status</th>
              <th className="px-3 py-1 font-medium">Linha do tempo</th>
              <th className="px-3 py-1 font-medium">Entrada</th>
              <th className="px-3 py-1 font-medium">Saída</th>
              <th className="px-3 py-1 text-right font-medium">Tarifa</th>
            </tr>
          </thead>
          <tbody>
            {tickets.map((ticket) => {
              const entry = ticket.events.find((event) => event.type === "ENTRY");
              const exit = ticket.events.find((event) => event.type === "EXIT");
              return (
                <tr key={ticket.id} className="bg-white/[0.02]">
                  <td className="rounded-l-lg px-3 py-2 font-mono text-slate-200">
                    {ticket.licensePlate}
                  </td>
                  <td className="px-3 py-2 text-slate-400">{ticket.sector ?? "—"}</td>
                  <td className="px-3 py-2">
                    <span
                      className={`rounded-md px-2 py-0.5 text-[11px] font-semibold ${statusStyle(ticket.status)}`}
                    >
                      {ticket.status}
                    </span>
                  </td>
                  <td className="px-3 py-2">
                    <Timeline ticket={ticket} />
                  </td>
                  <td className="px-3 py-2 tabular-nums text-slate-400">{clock(entry?.time)}</td>
                  <td className="px-3 py-2 tabular-nums text-slate-400">{clock(exit?.time)}</td>
                  <td className="rounded-r-lg px-3 py-2 text-right tabular-nums text-emerald-300">
                    {ticket.fare != null ? currency(ticket.fare) : "—"}
                  </td>
                </tr>
              );
            })}
            {tickets.length === 0 && (
              <tr>
                <td colSpan={7} className="px-3 py-8 text-center text-sm text-slate-500">
                  Nenhum ticket ainda.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
