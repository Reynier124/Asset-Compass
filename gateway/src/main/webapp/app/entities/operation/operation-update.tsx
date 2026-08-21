import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getAssets } from 'app/entities/asset/asset.reducer';
import { getEntities as getBrokerAccounts } from 'app/entities/broker-account/broker-account.reducer';
import { getEntities as getOperations } from 'app/entities/operation/operation.reducer';
import { OperationType } from 'app/shared/model/enumerations/operation-type.model';

import { createEntity, getEntity, reset, updateEntity } from './operation.reducer';

export const OperationUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const brokerAccounts = useAppSelector(state => state.gateway.brokerAccount.entities);
  const assets = useAppSelector(state => state.gateway.asset.entities);
  const operations = useAppSelector(state => state.gateway.operation.entities);
  const operationEntity = useAppSelector(state => state.gateway.operation.entity);
  const loading = useAppSelector(state => state.gateway.operation.loading);
  const updating = useAppSelector(state => state.gateway.operation.updating);
  const updateSuccess = useAppSelector(state => state.gateway.operation.updateSuccess);
  const operationTypeValues = Object.keys(OperationType);

  const handleClose = () => {
    navigate(`/operation${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getBrokerAccounts({}));
    dispatch(getAssets({}));
    dispatch(getOperations({}));
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
    if (values.price !== undefined && typeof values.price !== 'number') {
      values.price = Number(values.price);
    }
    if (values.amount !== undefined && typeof values.amount !== 'number') {
      values.amount = Number(values.amount);
    }
    if (values.underlyingPrice !== undefined && typeof values.underlyingPrice !== 'number') {
      values.underlyingPrice = Number(values.underlyingPrice);
    }
    if (values.commission !== undefined && typeof values.commission !== 'number') {
      values.commission = Number(values.commission);
    }

    const entity = {
      ...operationEntity,
      ...values,
      account: brokerAccounts.find(it => it.id.toString() === values.account?.toString()),
      asset: assets.find(it => it.id.toString() === values.asset?.toString()),
      closesOperation: operations.find(it => it.id.toString() === values.closesOperation?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          type: 'BUY',
          ...operationEntity,
          account: operationEntity?.account?.id,
          asset: operationEntity?.asset?.id,
          closesOperation: operationEntity?.closesOperation?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="gatewayApp.operation.home.createOrEditLabel" data-cy="OperationCreateUpdateHeading">
            <Translate contentKey="gatewayApp.operation.home.createOrEditLabel">Create or edit a Operation</Translate>
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
                  id="operation-id"
                  label={translate('gatewayApp.operation.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField label={translate('gatewayApp.operation.type')} id="operation-type" name="type" data-cy="type" type="select">
                {operationTypeValues.map(operationType => (
                  <option value={operationType} key={operationType}>
                    {translate(`gatewayApp.OperationType.${operationType}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('gatewayApp.operation.operationDate')}
                id="operation-operationDate"
                name="operationDate"
                data-cy="operationDate"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.operation.quantity')}
                id="operation-quantity"
                name="quantity"
                data-cy="quantity"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.operation.price')}
                id="operation-price"
                name="price"
                data-cy="price"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.operation.amount')}
                id="operation-amount"
                name="amount"
                data-cy="amount"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.operation.currency')}
                id="operation-currency"
                name="currency"
                data-cy="currency"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.operation.underlyingPrice')}
                id="operation-underlyingPrice"
                name="underlyingPrice"
                data-cy="underlyingPrice"
                type="text"
              />
              <ValidatedField
                label={translate('gatewayApp.operation.commission')}
                id="operation-commission"
                name="commission"
                data-cy="commission"
                type="text"
              />
              <ValidatedField
                id="operation-account"
                name="account"
                data-cy="account"
                label={translate('gatewayApp.operation.account')}
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
                id="operation-asset"
                name="asset"
                data-cy="asset"
                label={translate('gatewayApp.operation.asset')}
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
              <ValidatedField
                id="operation-closesOperation"
                name="closesOperation"
                data-cy="closesOperation"
                label={translate('gatewayApp.operation.closesOperation')}
                type="select"
              >
                <option value="" key="0" />
                {operations
                  ? operations.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/operation" replace variant="info">
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

export default OperationUpdate;
