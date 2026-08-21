import dayjs from 'dayjs';

import { IBrokerAccount } from 'app/shared/model/broker-account.model';

export interface IValuation {
  id?: string;
  snapshotDate?: dayjs.Dayjs;
  totalValue?: number;
  currency?: string;
  account?: IBrokerAccount | null;
}

export const defaultValue: Readonly<IValuation> = {};
