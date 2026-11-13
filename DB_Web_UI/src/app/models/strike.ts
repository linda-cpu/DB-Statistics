import { Lines } from "./lines";

export interface StrikeUI {
  affectedLines: Lines[];
  description: string;
  dateRange: Date[];
}

export interface StrikeRequest {
  affectedLines: Lines[];
  description: string;
  startTime: string;
  endTime: string;
}

export interface Strike {
  id: string;
  description: string;
  startTime: string;
  endTime: string;
  affectedLines: Lines[];
}
