import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getAssets } from 'app/entities/asset/asset.reducer';
import { getEntities as getBrokerAccounts } from 'app/entities/broker-account/broker-account.reducer';
import { IncomeType } from 'app/shared/model/enumerations/income-type.model';

import { createEntity, getEntity, reset, updateEntity } from './income-event.reducer';

export const IncomeEventUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const brokerAccounts = useAppSelector(state => state.gateway.brokerAccount.entities);
  const assets = useAppSelector(state => state.gateway.asset.entities);
  const incomeEventEntity = useAppSelector(state => state.gateway.incomeEvent.entity);
  const loading = useAppSelector(state => state.gateway.incomeEvent.loading);
  const updating = useAppSelector(state => state.gateway.incomeEvent.updating);
  const updateSuccess = useAppSelector(state => state.gateway.incomeEvent.updateSuccess);
  const incomeTypeValues = Object.keys(IncomeType);

  const handleClose = () => {
    navigate(`/income-event${location.search}`);
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
    if (values.amount !== undefined && typeof values.amount !== 'number') {
      values.amount = Number(values.amount);
    }

    const entity = {
      ...incomeEventEntity,
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
      ? {}
      : {
          type: 'DIVIDENDO',
          ...incomeEventEntity,
          account: incomeEventEntity?.account?.id,
          asset: incomeEventEntity?.asset?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="gatewayApp.incomeEvent.home.createOrEditLabel" data-cy="IncomeEventCreateUpdateHeading">
            <Translate contentKey="gatewayApp.incomeEvent.home.createOrEditLabel">Create or edit a IncomeEvent</Translate>
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
                  id="income-event-id"
                  label={translate('gatewayApp.incomeEvent.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('gatewayApp.incomeEvent.type')}
                id="income-event-type"
                name="type"
                data-cy="type"
                type="select"
              >
                {incomeTypeValues.map(incomeType => (
                  <option value={incomeType} key={incomeType}>
                    {translate(`gatewayApp.IncomeType.${incomeType}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('gatewayApp.incomeEvent.eventDate')}
                id="income-event-eventDate"
                name="eventDate"
                data-cy="eventDate"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.incomeEvent.amount')}
                id="income-event-amount"
                name="amount"
                data-cy="amount"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.incomeEvent.currency')}
                id="income-event-currency"
                name="currency"
                data-cy="currency"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                id="income-event-account"
                name="account"
                data-cy="account"
                label={translate('gatewayApp.incomeEvent.account')}
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
                id="income-event-asset"
                name="asset"
                data-cy="asset"
                label={translate('gatewayApp.incomeEvent.asset')}
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
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/income-event" replace variant="info">
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

export default IncomeEventUpdate;
