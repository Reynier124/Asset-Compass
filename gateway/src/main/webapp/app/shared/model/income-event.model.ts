import dayjs from 'dayjs';

import { IAsset } from 'app/shared/model/asset.model';
import { IBrokerAccount } from 'app/shared/model/broker-account.model';
import { IncomeType } from 'app/shared/model/enumerations/income-type.model';

export interface IIncomeEvent {
  id?: string;
  type?: keyof typeof IncomeType;
  eventDate?: dayjs.Dayjs;
  amount?: number;
  currency?: string;
  account?: IBrokerAccount;
  asset?: IAsset;
}

export const defaultValue: Readonly<IIncomeEvent> = {};
