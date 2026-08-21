export interface IAsset {
  id?: string;
  ticket?: string;
  category?: string | null;
  country?: string | null;
  description?: string | null;
}

export const defaultValue: Readonly<IAsset> = {};
