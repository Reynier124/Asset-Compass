import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './valuation.reducer';

export const ValuationDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const valuationEntity = useAppSelector(state => state.gateway.valuation.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="valuationDetailsHeading">
          <Translate contentKey="gatewayApp.valuation.detail.title">Valuation</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="gatewayApp.valuation.id">Id</Translate>
            </span>
          </dt>
          <dd>{valuationEntity.id}</dd>
          <dt>
            <span id="snapshotDate">
              <Translate contentKey="gatewayApp.valuation.snapshotDate">Snapshot Date</Translate>
            </span>
          </dt>
          <dd>
            {valuationEntity.snapshotDate ? (
              <TextFormat value={valuationEntity.snapshotDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="totalValue">
              <Translate contentKey="gatewayApp.valuation.totalValue">Total Value</Translate>
            </span>
          </dt>
          <dd>{valuationEntity.totalValue}</dd>
          <dt>
            <span id="currency">
              <Translate contentKey="gatewayApp.valuation.currency">Currency</Translate>
            </span>
          </dt>
          <dd>{valuationEntity.currency}</dd>
          <dt>
            <Translate contentKey="gatewayApp.valuation.account">Account</Translate>
          </dt>
          <dd>{valuationEntity.account ? valuationEntity.account.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/valuation" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/valuation/${valuationEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ValuationDetail;
