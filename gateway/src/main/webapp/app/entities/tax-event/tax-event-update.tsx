import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getBrokerAccounts } from 'app/entities/broker-account/broker-account.reducer';
import { getEntities as getIncomeEvents } from 'app/entities/income-event/income-event.reducer';
import { getEntities as getOperations } from 'app/entities/operation/operation.reducer';
import { TaxType } from 'app/shared/model/enumerations/tax-type.model';

import { createEntity, getEntity, reset, updateEntity } from './tax-event.reducer';

export const TaxEventUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const brokerAccounts = useAppSelector(state => state.gateway.brokerAccount.entities);
  const operations = useAppSelector(state => state.gateway.operation.entities);
  const incomeEvents = useAppSelector(state => state.gateway.incomeEvent.entities);
  const taxEventEntity = useAppSelector(state => state.gateway.taxEvent.entity);
  const loading = useAppSelector(state => state.gateway.taxEvent.loading);
  const updating = useAppSelector(state => state.gateway.taxEvent.updating);
  const updateSuccess = useAppSelector(state => state.gateway.taxEvent.updateSuccess);
  const taxTypeValues = Object.keys(TaxType);

  const handleClose = () => {
    navigate(`/tax-event${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getBrokerAccounts({}));
    dispatch(getOperations({}));
    dispatch(getIncomeEvents({}));
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
      ...taxEventEntity,
      ...values,
      account: brokerAccounts.find(it => it.id.toString() === values.account?.toString()),
      operation: operations.find(it => it.id.toString() === values.operation?.toString()),
      incomeEvent: incomeEvents.find(it => it.id.toString() === values.incomeEvent?.toString()),
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
          type: 'RETENCION_DIVIDENDO',
          ...taxEventEntity,
          account: taxEventEntity?.account?.id,
          operation: taxEventEntity?.operation?.id,
          incomeEvent: taxEventEntity?.incomeEvent?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="gatewayApp.taxEvent.home.createOrEditLabel" data-cy="TaxEventCreateUpdateHeading">
            <Translate contentKey="gatewayApp.taxEvent.home.createOrEditLabel">Create or edit a TaxEvent</Translate>
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
                  id="tax-event-id"
                  label={translate('gatewayApp.taxEvent.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField label={translate('gatewayApp.taxEvent.type')} id="tax-event-type" name="type" data-cy="type" type="select">
                {taxTypeValues.map(taxType => (
                  <option value={taxType} key={taxType}>
                    {translate(`gatewayApp.TaxType.${taxType}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('gatewayApp.taxEvent.taxDate')}
                id="tax-event-taxDate"
                name="taxDate"
                data-cy="taxDate"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.taxEvent.amount')}
                id="tax-event-amount"
                name="amount"
                data-cy="amount"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.taxEvent.currency')}
                id="tax-event-currency"
                name="currency"
                data-cy="currency"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                id="tax-event-account"
                name="account"
                data-cy="account"
                label={translate('gatewayApp.taxEvent.account')}
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
                id="tax-event-operation"
                name="operation"
                data-cy="operation"
                label={translate('gatewayApp.taxEvent.operation')}
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
              <ValidatedField
                id="tax-event-incomeEvent"
                name="incomeEvent"
                data-cy="incomeEvent"
                label={translate('gatewayApp.taxEvent.incomeEvent')}
                type="select"
              >
                <option value="" key="0" />
                {incomeEvents
                  ? incomeEvents.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/tax-event" replace variant="info">
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

export default TaxEventUpdate;
