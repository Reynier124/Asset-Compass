import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Valuation from './valuation';
import ValuationDeleteDialog from './valuation-delete-dialog';
import ValuationDetail from './valuation-detail';
import ValuationUpdate from './valuation-update';

const ValuationRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Valuation />} />
    <Route path="new" element={<ValuationUpdate />} />
    <Route path=":id">
      <Route index element={<ValuationDetail />} />
      <Route path="edit" element={<ValuationUpdate />} />
      <Route path="delete" element={<ValuationDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ValuationRoutes;
