import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './asset.reducer';

export const AssetDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const assetEntity = useAppSelector(state => state.gateway.asset.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="assetDetailsHeading">
          <Translate contentKey="gatewayApp.asset.detail.title">Asset</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="gatewayApp.asset.id">Id</Translate>
            </span>
          </dt>
          <dd>{assetEntity.id}</dd>
          <dt>
            <span id="ticket">
              <Translate contentKey="gatewayApp.asset.ticket">Ticket</Translate>
            </span>
          </dt>
          <dd>{assetEntity.ticket}</dd>
          <dt>
            <span id="category">
              <Translate contentKey="gatewayApp.asset.category">Category</Translate>
            </span>
          </dt>
          <dd>{assetEntity.category}</dd>
          <dt>
            <span id="country">
              <Translate contentKey="gatewayApp.asset.country">Country</Translate>
            </span>
          </dt>
          <dd>{assetEntity.country}</dd>
          <dt>
            <span id="description">
              <Translate contentKey="gatewayApp.asset.description">Description</Translate>
            </span>
          </dt>
          <dd>{assetEntity.description}</dd>
        </dl>
        <Button as={Link as any} to="/asset" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/asset/${assetEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default AssetDetail;
