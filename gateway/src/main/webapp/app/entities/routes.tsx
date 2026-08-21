import React from 'react';
import { Route } from 'react-router';

import { ReducersMapObject, combineReducers } from '@reduxjs/toolkit';

import getStore from 'app/config/store';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Asset from './asset';
import AssetRatio from './asset-ratio';
import Broker from './broker';
import BrokerAccount from './broker-account';
import IncomeEvent from './income-event';
import Operation from './operation';
import Position from './position';
import Rebate from './rebate';
import entitiesReducers from './reducers';
import TaxEvent from './tax-event';
import Valuation from './valuation';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  const store = getStore();
  store.injectReducer('gateway', combineReducers(entitiesReducers as ReducersMapObject));
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="/broker/*" element={<Broker />} />
        <Route path="/broker-account/*" element={<BrokerAccount />} />
        <Route path="/asset/*" element={<Asset />} />
        <Route path="/asset-ratio/*" element={<AssetRatio />} />
        <Route path="/operation/*" element={<Operation />} />
        <Route path="/rebate/*" element={<Rebate />} />
        <Route path="/tax-event/*" element={<TaxEvent />} />
        <Route path="/income-event/*" element={<IncomeEvent />} />
        <Route path="/position/*" element={<Position />} />
        <Route path="/valuation/*" element={<Valuation />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
