import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getAssets } from 'app/entities/asset/asset.reducer';
import { getEntities as getBrokerAccounts } from 'app/entities/broker-account/broker-account.reducer';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './position.reducer';

export const PositionUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const brokerAccounts = useAppSelector(state => state.gateway.brokerAccount.entities);
  const assets = useAppSelector(state => state.gateway.asset.entities);
  const positionEntity = useAppSelector(state => state.gateway.position.entity);
  const loading = useAppSelector(state => state.gateway.position.loading);
  const updating = useAppSelector(state => state.gateway.position.updating);
  const updateSuccess = useAppSelector(state => state.gateway.position.updateSuccess);

  const handleClose = () => {
    navigate(`/position${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getBrokerAccounts({}));
    dispatch(getAssets({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.quantity !== undefined && typeof values.quantity !== 'number') {
      values.quantity = Number(values.quantity);
    }
    if (values.averageCost !== undefined && typeof values.averageCost !== 'number') {
      values.averageCost = Number(values.averageCost);
    }
    if (values.currentValue !== undefined && typeof values.currentValue !== 'number') {
      values.currentValue = Number(values.currentValue);
    }
    values.lastSyncedAt = convertDateTimeToServer(values.lastSyncedAt);

    const entity = {
      ...positionEntity,
      ...values,
      account: brokerAccounts.find(it => it.id.toString() === values.account?.toString()),
      asset: assets.find(it => it.id.toString() === values.asset?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          lastSyncedAt: displayDefaultDateTime(),
        }
      : {
          ...positionEntity,
          lastSyncedAt: convertDateTimeFromServer(positionEntity.lastSyncedAt),
          account: positionEntity?.account?.id,
          asset: positionEntity?.asset?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="gatewayApp.position.home.createOrEditLabel" data-cy="PositionCreateUpdateHeading">
            <Translate contentKey="gatewayApp.position.home.createOrEditLabel">Create or edit a Position</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="position-id"
                  label={translate('gatewayApp.position.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('gatewayApp.position.quantity')}
                id="position-quantity"
                name="quantity"
                data-cy="quantity"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.position.averageCost')}
                id="position-averageCost"
                name="averageCost"
                data-cy="averageCost"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.position.currentValue')}
                id="position-currentValue"
                name="currentValue"
                data-cy="currentValue"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.position.currency')}
                id="position-currency"
                name="currency"
                data-cy="currency"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.position.lastSyncedAt')}
                id="position-lastSyncedAt"
                name="lastSyncedAt"
                data-cy="lastSyncedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                id="position-account"
                name="account"
                data-cy="account"
                label={translate('gatewayApp.position.account')}
                type="select"
                required
              >
                <option value="" key="0" />
                {brokerAccounts
                  ? brokerAccounts.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <ValidatedField
                id="position-asset"
                name="asset"
                data-cy="asset"
                label={translate('gatewayApp.position.asset')}
                type="select"
                required
              >
                <option value="" key="0" />
                {assets
                  ? assets.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/position" replace variant="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button variant="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default PositionUpdate;
