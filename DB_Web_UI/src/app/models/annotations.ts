import { WritableSignal } from "@angular/core"

export interface Annotations {
  id: string,
  source: string,
  text: string,
  code: number,
  changed_datetime: string,
  stop_id?: string
  name?: WritableSignal<string>
}
