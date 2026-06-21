export const EVENT_STYLES: Record<string, string> = {
  ENTRY: "bg-sky-500/15 text-sky-300 ring-1 ring-sky-500/30",
  PARKED: "bg-amber-500/15 text-amber-300 ring-1 ring-amber-500/30",
  EXIT: "bg-fuchsia-500/15 text-fuchsia-300 ring-1 ring-fuchsia-500/30",
};

export const STATUS_STYLES: Record<string, string> = {
  OPEN: "bg-emerald-500/15 text-emerald-300 ring-1 ring-emerald-500/30",
  CLOSED: "bg-slate-500/15 text-slate-300 ring-1 ring-slate-500/30",
};

export const DOT_STYLES: Record<string, string> = {
  ENTRY: "bg-sky-400",
  PARKED: "bg-amber-400",
  EXIT: "bg-fuchsia-400",
};

export const eventStyle = (type: string): string =>
  EVENT_STYLES[type] ?? "bg-slate-500/15 text-slate-300 ring-1 ring-slate-500/30";

export const dotStyle = (type: string): string => DOT_STYLES[type] ?? "bg-white/10";

export const statusStyle = (status: string): string =>
  STATUS_STYLES[status] ?? "bg-slate-500/15 text-slate-300 ring-1 ring-slate-500/30";
