import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Asset from './asset';
import AssetDeleteDialog from './asset-delete-dialog';
import AssetDetail from './asset-detail';
import AssetUpdate from './asset-update';

const AssetRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Asset />} />
    <Route path="new" element={<AssetUpdate />} />
    <Route path=":id">
      <Route index element={<AssetDetail />} />
      <Route path="edit" element={<AssetUpdate />} />
      <Route path="delete" element={<AssetDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default AssetRoutes;
