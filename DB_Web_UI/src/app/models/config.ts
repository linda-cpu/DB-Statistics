export interface Config {
  id?: number,
  title: string,
  chartType: "DELAY_HISTORY" | "TOP_DELAYED_LINES" | "TOP_DELAY_REASONS",
  stationEva: number,
  lineFilter: string,
  metricType: "PERCENT" | "COUNT",
  timeInterval: "DAILY" | "WEEKLY" | "MONTHLY" | "YEARLY",
  dateFrom: string,
  dateTo: string,
  limit: number,
}
