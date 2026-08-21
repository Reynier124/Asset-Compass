import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import IncomeEvent from './income-event';
import IncomeEventDeleteDialog from './income-event-delete-dialog';
import IncomeEventDetail from './income-event-detail';
import IncomeEventUpdate from './income-event-update';

const IncomeEventRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<IncomeEvent />} />
    <Route path="new" element={<IncomeEventUpdate />} />
    <Route path=":id">
      <Route index element={<IncomeEventDetail />} />
      <Route path="edit" element={<IncomeEventUpdate />} />
      <Route path="delete" element={<IncomeEventDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default IncomeEventRoutes;
