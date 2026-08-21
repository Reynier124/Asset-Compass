import { IBroker } from 'app/shared/model/broker.model';

export interface IBrokerAccount {
  id?: string;
  externalAccountId?: string;
  displayName?: string | null;
  broker?: IBroker;
}

export const defaultValue: Readonly<IBrokerAccount> = {};
