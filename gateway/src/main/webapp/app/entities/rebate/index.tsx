import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Rebate from './rebate';
import RebateDeleteDialog from './rebate-delete-dialog';
import RebateDetail from './rebate-detail';
import RebateUpdate from './rebate-update';

const RebateRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Rebate />} />
    <Route path="new" element={<RebateUpdate />} />
    <Route path=":id">
      <Route index element={<RebateDetail />} />
      <Route path="edit" element={<RebateUpdate />} />
      <Route path="delete" element={<RebateDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default RebateRoutes;
