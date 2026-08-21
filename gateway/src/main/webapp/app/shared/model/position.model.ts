import dayjs from 'dayjs';

import { IAsset } from 'app/shared/model/asset.model';
import { IBrokerAccount } from 'app/shared/model/broker-account.model';

export interface IPosition {
  id?: string;
  quantity?: number;
  averageCost?: number;
  currentValue?: number;
  currency?: string;
  lastSyncedAt?: dayjs.Dayjs;
  account?: IBrokerAccount;
  asset?: IAsset;
}

export const defaultValue: Readonly<IPosition> = {};
