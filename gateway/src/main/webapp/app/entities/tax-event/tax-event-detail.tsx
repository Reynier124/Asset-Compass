import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './tax-event.reducer';

export const TaxEventDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const taxEventEntity = useAppSelector(state => state.gateway.taxEvent.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="taxEventDetailsHeading">
          <Translate contentKey="gatewayApp.taxEvent.detail.title">TaxEvent</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="gatewayApp.taxEvent.id">Id</Translate>
            </span>
          </dt>
          <dd>{taxEventEntity.id}</dd>
          <dt>
            <span id="type">
              <Translate contentKey="gatewayApp.taxEvent.type">Type</Translate>
            </span>
          </dt>
          <dd>{taxEventEntity.type}</dd>
          <dt>
            <span id="taxDate">
              <Translate contentKey="gatewayApp.taxEvent.taxDate">Tax Date</Translate>
            </span>
          </dt>
          <dd>
            {taxEventEntity.taxDate ? <TextFormat value={taxEventEntity.taxDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="amount">
              <Translate contentKey="gatewayApp.taxEvent.amount">Amount</Translate>
            </span>
          </dt>
          <dd>{taxEventEntity.amount}</dd>
          <dt>
            <span id="currency">
              <Translate contentKey="gatewayApp.taxEvent.currency">Currency</Translate>
            </span>
          </dt>
          <dd>{taxEventEntity.currency}</dd>
          <dt>
            <Translate contentKey="gatewayApp.taxEvent.account">Account</Translate>
          </dt>
          <dd>{taxEventEntity.account ? taxEventEntity.account.id : ''}</dd>
          <dt>
            <Translate contentKey="gatewayApp.taxEvent.operation">Operation</Translate>
          </dt>
          <dd>{taxEventEntity.operation ? taxEventEntity.operation.id : ''}</dd>
          <dt>
            <Translate contentKey="gatewayApp.taxEvent.incomeEvent">Income Event</Translate>
          </dt>
          <dd>{taxEventEntity.incomeEvent ? taxEventEntity.incomeEvent.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/tax-event" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/tax-event/${taxEventEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default TaxEventDetail;
