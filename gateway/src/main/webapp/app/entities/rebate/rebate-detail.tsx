import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './rebate.reducer';

export const RebateDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const rebateEntity = useAppSelector(state => state.gateway.rebate.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="rebateDetailsHeading">
          <Translate contentKey="gatewayApp.rebate.detail.title">Rebate</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="gatewayApp.rebate.id">Id</Translate>
            </span>
          </dt>
          <dd>{rebateEntity.id}</dd>
          <dt>
            <span id="rebateDate">
              <Translate contentKey="gatewayApp.rebate.rebateDate">Rebate Date</Translate>
            </span>
          </dt>
          <dd>
            {rebateEntity.rebateDate ? <TextFormat value={rebateEntity.rebateDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="amount">
              <Translate contentKey="gatewayApp.rebate.amount">Amount</Translate>
            </span>
          </dt>
          <dd>{rebateEntity.amount}</dd>
          <dt>
            <span id="currency">
              <Translate contentKey="gatewayApp.rebate.currency">Currency</Translate>
            </span>
          </dt>
          <dd>{rebateEntity.currency}</dd>
          <dt>
            <Translate contentKey="gatewayApp.rebate.account">Account</Translate>
          </dt>
          <dd>{rebateEntity.account ? rebateEntity.account.id : ''}</dd>
          <dt>
            <Translate contentKey="gatewayApp.rebate.operation">Operation</Translate>
          </dt>
          <dd>{rebateEntity.operation ? rebateEntity.operation.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/rebate" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/rebate/${rebateEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default RebateDetail;
