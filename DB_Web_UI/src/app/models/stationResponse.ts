import { Station } from "./station"

export interface StationResponse {
  "content": Station[]
  "first": boolean,
  "last": boolean,
  "number": number,
  "size": number,
  "totalElements": number,
  "totalPages": number
}
