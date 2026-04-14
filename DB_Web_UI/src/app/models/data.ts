import { Datapoint } from './dataPoint';

export interface Data {
  datasets: [
    {
      label: string;
      data: Array<Datapoint>;
      backgroundColor?: string;
      borderRadius?: number;
    },
  ];
}
