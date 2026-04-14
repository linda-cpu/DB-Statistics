export interface Filter {
  station: string | number,
  statType: "DELAY_HISTORY" | "TOP_DELAYED_LINES" | "TOP_DELAY_REASONS" | "",
  range: string,
  line: string,
  metric: "PERCENT" | "COUNT",
  intervall: "DAILY" | "WEEKLY" | "MONTHLY" | "YEARLY",
  limit: number,
}
