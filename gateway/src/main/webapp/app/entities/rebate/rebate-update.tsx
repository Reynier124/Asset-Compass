import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getBrokerAccounts } from 'app/entities/broker-account/broker-account.reducer';
import { getEntities as getOperations } from 'app/entities/operation/operation.reducer';

import { createEntity, getEntity, reset, updateEntity } from './rebate.reducer';

export const RebateUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const brokerAccounts = useAppSelector(state => state.gateway.brokerAccount.entities);
  const operations = useAppSelector(state => state.gateway.operation.entities);
  const rebateEntity = useAppSelector(state => state.gateway.rebate.entity);
  const loading = useAppSelector(state => state.gateway.rebate.loading);
  const updating = useAppSelector(state => state.gateway.rebate.updating);
  const updateSuccess = useAppSelector(state => state.gateway.rebate.updateSuccess);

  const handleClose = () => {
    navigate(`/rebate${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getBrokerAccounts({}));
    dispatch(getOperations({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.amount !== undefined && typeof values.amount !== 'number') {
      values.amount = Number(values.amount);
    }

    const entity = {
      ...rebateEntity,
      ...values,
      account: brokerAccounts.find(it => it.id.toString() === values.account?.toString()),
      operation: operations.find(it => it.id.toString() === values.operation?.toString()),
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
          ...rebateEntity,
          account: rebateEntity?.account?.id,
          operation: rebateEntity?.operation?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="gatewayApp.rebate.home.createOrEditLabel" data-cy="RebateCreateUpdateHeading">
            <Translate contentKey="gatewayApp.rebate.home.createOrEditLabel">Create or edit a Rebate</Translate>
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
                  id="rebate-id"
                  label={translate('gatewayApp.rebate.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('gatewayApp.rebate.rebateDate')}
                id="rebate-rebateDate"
                name="rebateDate"
                data-cy="rebateDate"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.rebate.amount')}
                id="rebate-amount"
                name="amount"
                data-cy="amount"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.rebate.currency')}
                id="rebate-currency"
                name="currency"
                data-cy="currency"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                id="rebate-account"
                name="account"
                data-cy="account"
                label={translate('gatewayApp.rebate.account')}
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
                id="rebate-operation"
                name="operation"
                data-cy="operation"
                label={translate('gatewayApp.rebate.operation')}
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
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/rebate" replace variant="info">
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

export default RebateUpdate;
