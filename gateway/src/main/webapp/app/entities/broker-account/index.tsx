import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import BrokerAccount from './broker-account';
import BrokerAccountDeleteDialog from './broker-account-delete-dialog';
import BrokerAccountDetail from './broker-account-detail';
import BrokerAccountUpdate from './broker-account-update';

const BrokerAccountRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<BrokerAccount />} />
    <Route path="new" element={<BrokerAccountUpdate />} />
    <Route path=":id">
      <Route index element={<BrokerAccountDetail />} />
      <Route path="edit" element={<BrokerAccountUpdate />} />
      <Route path="delete" element={<BrokerAccountDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default BrokerAccountRoutes;
