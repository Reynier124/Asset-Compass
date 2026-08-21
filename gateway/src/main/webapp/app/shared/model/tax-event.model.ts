import dayjs from 'dayjs';

import { IBrokerAccount } from 'app/shared/model/broker-account.model';
import { TaxType } from 'app/shared/model/enumerations/tax-type.model';
import { IIncomeEvent } from 'app/shared/model/income-event.model';
import { IOperation } from 'app/shared/model/operation.model';

export interface ITaxEvent {
  id?: string;
  type?: keyof typeof TaxType;
  taxDate?: dayjs.Dayjs;
  amount?: number;
  currency?: string;
  account?: IBrokerAccount;
  operation?: IOperation | null;
  incomeEvent?: IIncomeEvent | null;
}

export const defaultValue: Readonly<ITaxEvent> = {};
