import asset from 'app/entities/asset/asset.reducer';
import assetRatio from 'app/entities/asset-ratio/asset-ratio.reducer';
import broker from 'app/entities/broker/broker.reducer';
import brokerAccount from 'app/entities/broker-account/broker-account.reducer';
import incomeEvent from 'app/entities/income-event/income-event.reducer';
import operation from 'app/entities/operation/operation.reducer';
import position from 'app/entities/position/position.reducer';
import rebate from 'app/entities/rebate/rebate.reducer';
import taxEvent from 'app/entities/tax-event/tax-event.reducer';
import valuation from 'app/entities/valuation/valuation.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  broker,
  brokerAccount,
  asset,
  assetRatio,
  operation,
  rebate,
  taxEvent,
  incomeEvent,
  position,
  valuation,
  // jhipster-needle-add-reducer-combine - JHipster will add reducer here
};

export default entitiesReducers;
