import { Trip } from "./trip";

export interface TripPage {
  content: Trip[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}
