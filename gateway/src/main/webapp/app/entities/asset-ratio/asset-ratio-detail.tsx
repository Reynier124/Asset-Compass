import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './asset-ratio.reducer';

export const AssetRatioDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const assetRatioEntity = useAppSelector(state => state.gateway.assetRatio.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="assetRatioDetailsHeading">
          <Translate contentKey="gatewayApp.assetRatio.detail.title">AssetRatio</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="gatewayApp.assetRatio.id">Id</Translate>
            </span>
          </dt>
          <dd>{assetRatioEntity.id}</dd>
          <dt>
            <span id="ratio">
              <Translate contentKey="gatewayApp.assetRatio.ratio">Ratio</Translate>
            </span>
          </dt>
          <dd>{assetRatioEntity.ratio}</dd>
          <dt>
            <span id="effectiveFrom">
              <Translate contentKey="gatewayApp.assetRatio.effectiveFrom">Effective From</Translate>
            </span>
          </dt>
          <dd>
            {assetRatioEntity.effectiveFrom ? (
              <TextFormat value={assetRatioEntity.effectiveFrom} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <Translate contentKey="gatewayApp.assetRatio.asset">Asset</Translate>
          </dt>
          <dd>{assetRatioEntity.asset ? assetRatioEntity.asset.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/asset-ratio" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/asset-ratio/${assetRatioEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default AssetRatioDetail;
