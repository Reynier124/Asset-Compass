import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './operation.reducer';

export const OperationDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const operationEntity = useAppSelector(state => state.gateway.operation.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="operationDetailsHeading">
          <Translate contentKey="gatewayApp.operation.detail.title">Operation</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="gatewayApp.operation.id">Id</Translate>
            </span>
          </dt>
          <dd>{operationEntity.id}</dd>
          <dt>
            <span id="type">
              <Translate contentKey="gatewayApp.operation.type">Type</Translate>
            </span>
          </dt>
          <dd>{operationEntity.type}</dd>
          <dt>
            <span id="operationDate">
              <Translate contentKey="gatewayApp.operation.operationDate">Operation Date</Translate>
            </span>
          </dt>
          <dd>
            {operationEntity.operationDate ? (
              <TextFormat value={operationEntity.operationDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="quantity">
              <Translate contentKey="gatewayApp.operation.quantity">Quantity</Translate>
            </span>
          </dt>
          <dd>{operationEntity.quantity}</dd>
          <dt>
            <span id="price">
              <Translate contentKey="gatewayApp.operation.price">Price</Translate>
            </span>
          </dt>
          <dd>{operationEntity.price}</dd>
          <dt>
            <span id="amount">
              <Translate contentKey="gatewayApp.operation.amount">Amount</Translate>
            </span>
          </dt>
          <dd>{operationEntity.amount}</dd>
          <dt>
            <span id="currency">
              <Translate contentKey="gatewayApp.operation.currency">Currency</Translate>
            </span>
          </dt>
          <dd>{operationEntity.currency}</dd>
          <dt>
            <span id="underlyingPrice">
              <Translate contentKey="gatewayApp.operation.underlyingPrice">Underlying Price</Translate>
            </span>
          </dt>
          <dd>{operationEntity.underlyingPrice}</dd>
          <dt>
            <span id="commission">
              <Translate contentKey="gatewayApp.operation.commission">Commission</Translate>
            </span>
          </dt>
          <dd>{operationEntity.commission}</dd>
          <dt>
            <Translate contentKey="gatewayApp.operation.account">Account</Translate>
          </dt>
          <dd>{operationEntity.account ? operationEntity.account.id : ''}</dd>
          <dt>
            <Translate contentKey="gatewayApp.operation.asset">Asset</Translate>
          </dt>
          <dd>{operationEntity.asset ? operationEntity.asset.id : ''}</dd>
          <dt>
            <Translate contentKey="gatewayApp.operation.closesOperation">Closes Operation</Translate>
          </dt>
          <dd>{operationEntity.closesOperation ? operationEntity.closesOperation.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/operation" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/operation/${operationEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default OperationDetail;
