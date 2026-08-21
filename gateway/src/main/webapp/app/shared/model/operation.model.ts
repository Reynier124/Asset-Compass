import dayjs from 'dayjs';

import { IAsset } from 'app/shared/model/asset.model';
import { IBrokerAccount } from 'app/shared/model/broker-account.model';
import { OperationType } from 'app/shared/model/enumerations/operation-type.model';

export interface IOperation {
  id?: string;
  type?: keyof typeof OperationType;
  operationDate?: dayjs.Dayjs;
  quantity?: number;
  price?: number;
  amount?: number;
  currency?: string;
  underlyingPrice?: number | null;
  commission?: number | null;
  account?: IBrokerAccount;
  asset?: IAsset;
  closesOperation?: IOperation | null;
}

export const defaultValue: Readonly<IOperation> = {};
