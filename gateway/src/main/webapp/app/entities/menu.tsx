import React from 'react';
import { Translate } from 'react-jhipster';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/broker">
        <Translate contentKey="global.menu.entities.broker" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/broker-account">
        <Translate contentKey="global.menu.entities.brokerAccount" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/asset">
        <Translate contentKey="global.menu.entities.asset" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/asset-ratio">
        <Translate contentKey="global.menu.entities.assetRatio" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/operation">
        <Translate contentKey="global.menu.entities.operation" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/rebate">
        <Translate contentKey="global.menu.entities.rebate" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/tax-event">
        <Translate contentKey="global.menu.entities.taxEvent" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/income-event">
        <Translate contentKey="global.menu.entities.incomeEvent" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/position">
        <Translate contentKey="global.menu.entities.position" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/valuation">
        <Translate contentKey="global.menu.entities.valuation" />
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
