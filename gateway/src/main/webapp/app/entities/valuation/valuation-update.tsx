import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getBrokerAccounts } from 'app/entities/broker-account/broker-account.reducer';

import { createEntity, getEntity, reset, updateEntity } from './valuation.reducer';

export const ValuationUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const brokerAccounts = useAppSelector(state => state.gateway.brokerAccount.entities);
  const valuationEntity = useAppSelector(state => state.gateway.valuation.entity);
  const loading = useAppSelector(state => state.gateway.valuation.loading);
  const updating = useAppSelector(state => state.gateway.valuation.updating);
  const updateSuccess = useAppSelector(state => state.gateway.valuation.updateSuccess);

  const handleClose = () => {
    navigate(`/valuation${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getBrokerAccounts({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.totalValue !== undefined && typeof values.totalValue !== 'number') {
      values.totalValue = Number(values.totalValue);
    }

    const entity = {
      ...valuationEntity,
      ...values,
      account: brokerAccounts.find(it => it.id.toString() === values.account?.toString()),
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
          ...valuationEntity,
          account: valuationEntity?.account?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="gatewayApp.valuation.home.createOrEditLabel" data-cy="ValuationCreateUpdateHeading">
            <Translate contentKey="gatewayApp.valuation.home.createOrEditLabel">Create or edit a Valuation</Translate>
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
                  id="valuation-id"
                  label={translate('gatewayApp.valuation.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('gatewayApp.valuation.snapshotDate')}
                id="valuation-snapshotDate"
                name="snapshotDate"
                data-cy="snapshotDate"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.valuation.totalValue')}
                id="valuation-totalValue"
                name="totalValue"
                data-cy="totalValue"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('gatewayApp.valuation.currency')}
                id="valuation-currency"
                name="currency"
                data-cy="currency"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                id="valuation-account"
                name="account"
                data-cy="account"
                label={translate('gatewayApp.valuation.account')}
                type="select"
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
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/valuation" replace variant="info">
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

export default ValuationUpdate;
