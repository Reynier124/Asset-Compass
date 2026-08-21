import dayjs from 'dayjs';

import { IAsset } from 'app/shared/model/asset.model';

export interface IAssetRatio {
  id?: string;
  ratio?: string;
  effectiveFrom?: dayjs.Dayjs;
  asset?: IAsset;
}

export const defaultValue: Readonly<IAssetRatio> = {};
