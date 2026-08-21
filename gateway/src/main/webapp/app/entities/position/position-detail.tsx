import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './position.reducer';

export const PositionDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const positionEntity = useAppSelector(state => state.gateway.position.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="positionDetailsHeading">
          <Translate contentKey="gatewayApp.position.detail.title">Position</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="gatewayApp.position.id">Id</Translate>
            </span>
          </dt>
          <dd>{positionEntity.id}</dd>
          <dt>
            <span id="quantity">
              <Translate contentKey="gatewayApp.position.quantity">Quantity</Translate>
            </span>
          </dt>
          <dd>{positionEntity.quantity}</dd>
          <dt>
            <span id="averageCost">
              <Translate contentKey="gatewayApp.position.averageCost">Average Cost</Translate>
            </span>
          </dt>
          <dd>{positionEntity.averageCost}</dd>
          <dt>
            <span id="currentValue">
              <Translate contentKey="gatewayApp.position.currentValue">Current Value</Translate>
            </span>
          </dt>
          <dd>{positionEntity.currentValue}</dd>
          <dt>
            <span id="currency">
              <Translate contentKey="gatewayApp.position.currency">Currency</Translate>
            </span>
          </dt>
          <dd>{positionEntity.currency}</dd>
          <dt>
            <span id="lastSyncedAt">
              <Translate contentKey="gatewayApp.position.lastSyncedAt">Last Synced At</Translate>
            </span>
          </dt>
          <dd>
            {positionEntity.lastSyncedAt ? <TextFormat value={positionEntity.lastSyncedAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <Translate contentKey="gatewayApp.position.account">Account</Translate>
          </dt>
          <dd>{positionEntity.account ? positionEntity.account.id : ''}</dd>
          <dt>
            <Translate contentKey="gatewayApp.position.asset">Asset</Translate>
          </dt>
          <dd>{positionEntity.asset ? positionEntity.asset.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/position" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/position/${positionEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default PositionDetail;
