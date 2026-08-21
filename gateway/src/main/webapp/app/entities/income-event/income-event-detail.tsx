import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './income-event.reducer';

export const IncomeEventDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const incomeEventEntity = useAppSelector(state => state.gateway.incomeEvent.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="incomeEventDetailsHeading">
          <Translate contentKey="gatewayApp.incomeEvent.detail.title">IncomeEvent</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="gatewayApp.incomeEvent.id">Id</Translate>
            </span>
          </dt>
          <dd>{incomeEventEntity.id}</dd>
          <dt>
            <span id="type">
              <Translate contentKey="gatewayApp.incomeEvent.type">Type</Translate>
            </span>
          </dt>
          <dd>{incomeEventEntity.type}</dd>
          <dt>
            <span id="eventDate">
              <Translate contentKey="gatewayApp.incomeEvent.eventDate">Event Date</Translate>
            </span>
          </dt>
          <dd>
            {incomeEventEntity.eventDate ? (
              <TextFormat value={incomeEventEntity.eventDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="amount">
              <Translate contentKey="gatewayApp.incomeEvent.amount">Amount</Translate>
            </span>
          </dt>
          <dd>{incomeEventEntity.amount}</dd>
          <dt>
            <span id="currency">
              <Translate contentKey="gatewayApp.incomeEvent.currency">Currency</Translate>
            </span>
          </dt>
          <dd>{incomeEventEntity.currency}</dd>
          <dt>
            <Translate contentKey="gatewayApp.incomeEvent.account">Account</Translate>
          </dt>
          <dd>{incomeEventEntity.account ? incomeEventEntity.account.id : ''}</dd>
          <dt>
            <Translate contentKey="gatewayApp.incomeEvent.asset">Asset</Translate>
          </dt>
          <dd>{incomeEventEntity.asset ? incomeEventEntity.asset.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/income-event" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/income-event/${incomeEventEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default IncomeEventDetail;
