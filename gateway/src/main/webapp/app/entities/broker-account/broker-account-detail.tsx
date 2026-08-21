import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './broker-account.reducer';

export const BrokerAccountDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const brokerAccountEntity = useAppSelector(state => state.gateway.brokerAccount.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="brokerAccountDetailsHeading">
          <Translate contentKey="gatewayApp.brokerAccount.detail.title">BrokerAccount</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="gatewayApp.brokerAccount.id">Id</Translate>
            </span>
          </dt>
          <dd>{brokerAccountEntity.id}</dd>
          <dt>
            <span id="externalAccountId">
              <Translate contentKey="gatewayApp.brokerAccount.externalAccountId">External Account Id</Translate>
            </span>
          </dt>
          <dd>{brokerAccountEntity.externalAccountId}</dd>
          <dt>
            <span id="displayName">
              <Translate contentKey="gatewayApp.brokerAccount.displayName">Display Name</Translate>
            </span>
          </dt>
          <dd>{brokerAccountEntity.displayName}</dd>
          <dt>
            <Translate contentKey="gatewayApp.brokerAccount.broker">Broker</Translate>
          </dt>
          <dd>{brokerAccountEntity.broker ? brokerAccountEntity.broker.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/broker-account" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/broker-account/${brokerAccountEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default BrokerAccountDetail;
