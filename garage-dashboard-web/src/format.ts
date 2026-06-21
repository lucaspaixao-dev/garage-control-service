const brl = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

export const currency = (value: number | null | undefined): string => brl.format(value ?? 0);

export const percent = (rate: number): string => `${Math.round(rate * 100)}%`;

export const clock = (iso: string | null | undefined): string =>
  iso
    ? new Date(iso).toLocaleTimeString("pt-BR", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      })
    : "—";

export function ago(iso: string): string {
  const seconds = Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 1000));
  if (seconds < 60) return `${seconds}s atrás`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}min atrás`;
  return `${Math.floor(minutes / 60)}h atrás`;
}
