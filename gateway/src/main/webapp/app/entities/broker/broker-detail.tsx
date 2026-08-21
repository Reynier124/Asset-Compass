import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './broker.reducer';

export const BrokerDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const brokerEntity = useAppSelector(state => state.gateway.broker.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="brokerDetailsHeading">
          <Translate contentKey="gatewayApp.broker.detail.title">Broker</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="gatewayApp.broker.id">Id</Translate>
            </span>
          </dt>
          <dd>{brokerEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="gatewayApp.broker.name">Name</Translate>
            </span>
          </dt>
          <dd>{brokerEntity.name}</dd>
        </dl>
        <Button as={Link as any} to="/broker" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/broker/${brokerEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default BrokerDetail;
