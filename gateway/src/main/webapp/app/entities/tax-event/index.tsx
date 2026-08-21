import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import TaxEvent from './tax-event';
import TaxEventDeleteDialog from './tax-event-delete-dialog';
import TaxEventDetail from './tax-event-detail';
import TaxEventUpdate from './tax-event-update';

const TaxEventRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<TaxEvent />} />
    <Route path="new" element={<TaxEventUpdate />} />
    <Route path=":id">
      <Route index element={<TaxEventDetail />} />
      <Route path="edit" element={<TaxEventUpdate />} />
      <Route path="delete" element={<TaxEventDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default TaxEventRoutes;
