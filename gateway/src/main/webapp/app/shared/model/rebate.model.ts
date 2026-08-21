import dayjs from 'dayjs';

import { IBrokerAccount } from 'app/shared/model/broker-account.model';
import { IOperation } from 'app/shared/model/operation.model';

export interface IRebate {
  id?: string;
  rebateDate?: dayjs.Dayjs;
  amount?: number;
  currency?: string;
  account?: IBrokerAccount;
  operation?: IOperation | null;
}

export const defaultValue: Readonly<IRebate> = {};
