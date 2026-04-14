import { Annotations } from "./annotations";

export interface Trip {
  id: string;
  station: string;
  schedule: {
    arrival_plan: string;
    arrival_real: string;
    depature_plan: string;
    depature_real: string;
    plattform_plan: string;
    plattform_real: string;
  };
  train_info: {
    line: string;
    id: string;
  }
  annotations: Annotations[];
}
