import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import AssetRatio from './asset-ratio';
import AssetRatioDeleteDialog from './asset-ratio-delete-dialog';
import AssetRatioDetail from './asset-ratio-detail';
import AssetRatioUpdate from './asset-ratio-update';

const AssetRatioRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<AssetRatio />} />
    <Route path="new" element={<AssetRatioUpdate />} />
    <Route path=":id">
      <Route index element={<AssetRatioDetail />} />
      <Route path="edit" element={<AssetRatioUpdate />} />
      <Route path="delete" element={<AssetRatioDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default AssetRatioRoutes;
